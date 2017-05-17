package com.three.nsq.commom;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.three.nsq.trendrr.*;
import com.three.nsq.trendrr.lookup.NSQLookupDynMapImpl;
import com.three.utils.ServerUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NsqService implements ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private NSQProducer producer;

    private ApplicationContext applicationContext;

    private String nsqd;

    private String nsqlookupd;


    private String nsqStageTopic;

    private ExecutorService nsqExecutorService = new ThreadPoolExecutor(4,
            8,
            1,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(200),
            new ThreadFactoryBuilder().setNameFormat("nsq-producer-thread-%d").build(),
            (r, executor) -> {
                if (r instanceof NsqTask) {
                    NsqTask task = (NsqTask) r;
                    logger.error("NSQProductReject:topic[{}],message[{}]", task.getTopic(), new String(task.getMessage()));
                }
            });

    private class NsqTask implements Runnable {
        private String topic;
        private byte[] message;

        NsqTask(String topic, byte[] message) {
            this.topic = topic;
            this.message = message;
        }

        public String getTopic() {
            return topic;
        }

        public byte[] getMessage() {
            return message;
        }

        @Override
        public void run() {
            doProduce(topic, message);
        }
    }

    public final static String EPHEMERAL = "#ephemeral";

    @PostConstruct
    public void init() {
        producer = new NSQProducer();
        for (String server : nsqd.split(" ")) {
            logger.warn("producer: {}", server);
            producer.addAddress(server, 4150, 10);
        }
        producer.start();

        String channelSuffix = "_" + ServerUtils.getHostAddress(0);
        logger.warn("channelSuffix ip : {}", channelSuffix);

        NSQLookup lookup = new NSQLookupDynMapImpl();
        for (String server : nsqlookupd.split(" ")) {
            lookup.addAddr(server, 4161);
        }

        Reflections reflections = new Reflections("com.kugou", new MethodAnnotationsScanner());
        for (final Method method : reflections.getMethodsAnnotatedWith(NsqCallback.class)) {
            Class<?> clazz = method.getDeclaringClass();
            Collection<?> beans = applicationContext.getBeansOfType(clazz).values();
            int size = beans.size();
            if (size == 0) {
                logger.error("find NsqCallback but it's not spring bean: {}.{}", clazz, method.getName());
                continue;
            } else if (size == 1) {
            } else {
                logger.error("find NsqCallback but more than one spring bean: {}.{}", clazz, method.getName());
                continue;
            }

            NsqCallback callback = method.getAnnotation(NsqCallback.class);
            final Object bean = beans.iterator().next();
            String channel = callback.channel();
            //测试环境用广播+NsqTopic.EPHEMERAL
            if (!callback.onlyChannel()) {
                channel = channel + channelSuffix;
            }
            NSQConsumer consumer = newNsqConsumer(lookup, callback.topic(), channel, bean, method);
            consumer.start();
            logger.warn("current process  bind nsq channel is: {}", channel);
            logger.warn("find NsqCallback: {}.{}, {}", clazz, method.getName(), callback);
        }

    }

    private NSQConsumer newNsqConsumer(final NSQLookup lookup, final String topic, final String channel, final Object bean, final Method method) {
        final String nsqCatTransactionName = "consume-" + topic + "-" + channel;
        return new NSQConsumer(lookup, topic, channel, new NSQMessageCallback() {
            @Override
            public void message(NSQMessage message) {
                try {
                    method.invoke(bean, message);
                } catch (Exception e) {
                    logger.error("consume error, topic:{}, channel:{}", topic, channel);
                    logger.error("", e);
                }
            }
        });
    }

    /**
     * 异步生产nsq消息
     *
     * @param topic   nsqTopic
     * @param message nsqMessage
     */
    public void produce(String topic, byte[] message) {
        nsqExecutorService.execute(new NsqTask(topic, message));
    }


    /**
     * 同步生产nsq消息
     *
     * @param topic   nsqTopic
     * @param message nsqMessage
     */
    public void syncProduce(String topic, byte[] message) {
        doProduce(topic, message);
    }


    private void doProduce(String topic, byte[] message) {
        try {
            producer.produce(topic, message);
            logger.info("NSQProductInfo:topic[{}],message[{}]", topic, new String(message));
        } catch (Exception e) {
            FormattingTuple formattingTuple = MessageFormatter.arrayFormat("NSQProductError:topic[{}],message[{}]", new Object[]{topic, new String(message)});
            logger.error(formattingTuple.getMessage(), e);
        }
    }

    public void setNsqd(String nsqd) {
        this.nsqd = nsqd;
    }

    public void setNsqlookupd(String nsqlookupd) {
        this.nsqlookupd = nsqlookupd;
    }

    public void setNsqStageTopic(String nsqStageTopic) {
        this.nsqStageTopic = nsqStageTopic;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
