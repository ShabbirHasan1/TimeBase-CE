package com.epam.deltix.test.qsrv.hf.tickdb.topicdemo.testmode;

import com.epam.deltix.qsrv.hf.pub.ChannelPerformance;
import com.epam.deltix.test.qsrv.hf.tickdb.topicdemo.util.ExperimentFormat;
import com.epam.deltix.test.qsrv.hf.tickdb.topicdemo.*;

/**
 * @author Alexei Osipov
 */
public enum CommunicationType implements TestComponentProvider {
    TOPIC {
        @Override
        public ReadAndReplyBase getReader() {
            return new ReadAndReplyTopic();
        }

        @Override
        public WriteBase getWriter() {
            return new WriteTopic();
        }

        @Override
        public ReadEchoBase getEchoReader(ExperimentFormat experimentFormat) {
            return new ReadEchoTopic(experimentFormat);
        }
    },

    SOCKET_STREAM {
        @Override
        public ReadAndReplyBase getReader() {
            return new ReadAndReplyStream(ChannelPerformance.LOW_LATENCY);
        }

        @Override
        public WriteBase getWriter() {
            return new WriteStream(ChannelPerformance.LOW_LATENCY);
        }

        @Override
        public ReadEchoBase getEchoReader(ExperimentFormat experimentFormat) {
            return new ReadEchoStream(ChannelPerformance.LOW_LATENCY, experimentFormat);
        }
    },

    IPC_STREAM {
        @Override
        public ReadAndReplyBase getReader() {
            return new ReadAndReplyStream(ChannelPerformance.LATENCY_CRITICAL);
        }

        @Override
        public WriteBase getWriter() {
            return new WriteStream(ChannelPerformance.LATENCY_CRITICAL);
        }

        @Override
        public ReadEchoBase getEchoReader(ExperimentFormat experimentFormat) {
            return new ReadEchoStream(ChannelPerformance.LATENCY_CRITICAL, experimentFormat);
        }
    }
}
