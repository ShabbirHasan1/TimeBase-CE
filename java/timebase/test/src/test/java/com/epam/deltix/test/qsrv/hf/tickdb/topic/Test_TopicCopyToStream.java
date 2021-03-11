package com.epam.deltix.test.qsrv.hf.tickdb.topic;

import com.epam.deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import com.epam.deltix.qsrv.hf.tickdb.pub.DXTickDB;
import com.epam.deltix.qsrv.hf.tickdb.pub.DXTickStream;
import com.epam.deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import com.epam.deltix.qsrv.hf.tickdb.pub.TickCursor;
import com.epam.deltix.qsrv.hf.tickdb.pub.topic.TopicDB;
import com.epam.deltix.qsrv.hf.tickdb.pub.topic.settings.TopicSettings;
import com.epam.deltix.util.JUnitCategories;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alexei Osipov
 */
@Category(JUnitCategories.TickDB.class)
public class Test_TopicCopyToStream extends Test_TopicPollerBase {
    private String copyToStreamKey;

    @Test(timeout = TEST_TIMEOUT + 30_000)
    //@Ignore // This test can create a significant amount of data (like 1Gb) and put big pressure on the disk drive. So it's disabled by default.
    public void test() throws Exception {
        DXTickDB tickDb = runner.getTickDb();
        executeTest();
        DXTickStream stream = tickDb.getStream(copyToStreamKey);
        try {
            TickCursor cursor = stream.createCursor(new SelectionOptions());
            cursor.subscribeToAllEntities();
            cursor.subscribeToAllTypes();
            cursor.reset(Long.MIN_VALUE);
            long count = 0;
            while (cursor.next()) {
                count++;
            }
            cursor.close();
            Assert.assertEquals(this.finalMessageSentCount.longValue(), count);
        } finally {
            stream.delete();
        }
    }

    @Override
    protected void createTopic(TopicDB topicDB, String topicKey, RecordClassDescriptor[] types) {
        this.copyToStreamKey = "stream:" + topicKey;
        topicDB.createTopic(topicKey, types,  new TopicSettings().setCopyToStream(copyToStreamKey));
    }
}