package com.epam.deltix.qsrv.hf.tickdb.impl;

import com.epam.deltix.streaming.MessageChannel;
import com.epam.deltix.timebase.messages.InstrumentMessage;
import com.epam.deltix.qsrv.hf.pub.codec.TimeCodec;
import com.epam.deltix.data.stream.*;
import com.epam.deltix.qsrv.hf.tickdb.pub.WriterAbortedException;
import com.epam.deltix.qsrv.hf.tickdb.pub.WriterClosedException;
import com.epam.deltix.timebase.messages.TimeStampedMessage;
import com.epam.deltix.util.concurrent.UnavailableResourceException;
import com.epam.deltix.util.memory.MemoryDataOutput;
import com.epam.deltix.util.time.TimeKeeper;

/**
 *
 */
abstract class MessageQueueWriter <Q extends MessageQueue> 
    implements MessageChannel<InstrumentMessage>
{
    private final TransientStreamImpl                       stream;    
    protected final Q                                       queue;
    protected final MemoryDataOutput                        buffer =
        new MemoryDataOutput (4096);
    private final MessageEncoder <InstrumentMessage>        encoder;
    private volatile  boolean                               isOpen = true;
    private long                                            timestamp;
    
    MessageQueueWriter (
        TransientStreamImpl                         stream,
        Q                                           queue,
        MessageEncoder <InstrumentMessage>          encoder
    )
    {
        this.stream = stream;
        this.queue = queue;
        this.encoder = encoder;
    }

    private void                    writeBuffer(int offset)
        throws UnavailableResourceException
    {
        queue.writeMessage (
            timestamp,
            buffer.getBuffer (),
            offset,
            buffer.getSize () - offset
        );
    }

    protected void                  writeBuffer (long time) {
        if (time == TimeStampedMessage.TIMESTAMP_UNKNOWN) {
            synchronized (queue) {
                long nanoTime = TimeKeeper.currentTimeNanos;

                int size = TimeCodec.getFieldSize(nanoTime);
                int offset = TimeCodec.MAX_SIZE - size;
                buffer.seek(offset);
                TimeCodec.writeNanoTime (nanoTime, buffer);
                writeBuffer(offset);
            }
        } else {
            writeBuffer(0);
        }
    }

    /**
     * Attempts to serialize message to buffer.
     *
     * @return true if message was written and false if message was rejected
     */
    protected final boolean         prepare (InstrumentMessage msg) {
        if (!isOpen)
            throw new WriterClosedException (this + " is closed");

        if (queue.closed)
            throw new WriterAbortedException(stream + " is closed");

        if (!stream.accumulateIfRequired (msg))
            return (false);

        timestamp = msg.getTimeStampMs();
        buffer.reset ();

        if (timestamp == TimeStampedMessage.TIMESTAMP_UNKNOWN)
            buffer.skip(TimeCodec.MAX_SIZE);
        else
            TimeCodec.writeTime (msg, buffer);
        
        encoder.encode (msg, buffer);
        
        return (true);
    }

    public void                     close () {
        isOpen = false;
    }
}
