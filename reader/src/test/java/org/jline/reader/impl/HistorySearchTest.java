package org.jline.reader.impl;

import java.io.ByteArrayInputStream;

import org.jline.reader.LineReader;
import org.jline.reader.impl.history.DefaultHistory;
import org.junit.Test;

import static org.jline.keymap.KeyMap.translate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HistorySearchTest extends ReaderTestSupport {

    private DefaultHistory setupHistory() {
        DefaultHistory history = new DefaultHistory();
        reader.setVariable(LineReader.HISTORY_SIZE, 10);
        reader.setHistory(history);
        history.add("foo");
        history.add("fiddle");
        history.add("faddle");
        return history;
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        setupHistory();
        reader.setOpt(LineReader.Option.CASE_INSENSITIVE_SEARCH);
        try {
            assertLine("fiddle", new TestBuffer().ctrl('R').append("I").enter(), false);
        } finally {
            reader.unsetOpt(LineReader.Option.CASE_INSENSITIVE_SEARCH);
        }
    }

    @Test
    public void testReverseHistorySearch() throws Exception {
        DefaultHistory history = setupHistory();

        // TODO: use assertBuffer
        String readLineResult;
        in.setIn(new ByteArrayInputStream(translate("^Rf\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("faddle", readLineResult);
        assertEquals(3, history.size());

        in.setIn(new ByteArrayInputStream(translate("^Rf^R^R^R^R^R\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("foo", readLineResult);
        assertEquals(4, history.size());

        in.setIn(new ByteArrayInputStream(translate("^Rf^R^R\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(5, history.size());
    }

    @Test
    public void testForwardHistorySearch() throws Exception {
        DefaultHistory history = setupHistory();

        String readLineResult;
        in.setIn(new ByteArrayInputStream(translate("^Rf^R^R^S\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(4, history.size());

        in.setIn(new ByteArrayInputStream(translate("^Rf^R^R^R^S^S\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("faddle", readLineResult);
        assertEquals(5, history.size());

        in.setIn(new ByteArrayInputStream(translate("^Rf^R^R^R^R^S\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(6, history.size());
    }

    @Test
    public void testSearchHistoryAfterHittingEnd() throws Exception {
        DefaultHistory history = setupHistory();

        String readLineResult;
        in.setIn(new ByteArrayInputStream(translate("^Rf^R^R^R^S\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(4, history.size());
    }

    @Test
    public void testSearchHistoryWithNoMatches() throws Exception {
        DefaultHistory history = setupHistory();

        String readLineResult;
        in.setIn(new ByteArrayInputStream(translate("x^S^S\n").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("x", readLineResult);
        assertEquals(4, history.size());
    }

    @Test
    public void testAbortingSearchRetainsCurrentBufferAndPrintsDetails() throws Exception {
        DefaultHistory history = setupHistory();

        String readLineResult;
        in.setIn(new ByteArrayInputStream(translate("f^Rf^G").getBytes()));
        readLineResult = reader.readLine();
        assertEquals(null, readLineResult);
        assertEquals("f", reader.getBuffer().toString());
        assertEquals(3, history.size());
    }

    @Test
    public void testAbortingAfterSearchingPreviousLinesGivesBlank() throws Exception {
        DefaultHistory history = setupHistory();

        String readLineResult;
        in.setIn(new ByteArrayInputStream(translate("f^Rf\nfoo^G").getBytes()));
        readLineResult = reader.readLine();
        assertEquals("f", readLineResult);
        assertEquals(4, history.size());

        readLineResult = reader.readLine();
        assertEquals(null, readLineResult);
        assertEquals("", reader.getBuffer().toString());
        assertEquals(4, history.size());
    }

    @Test
    public void testSearchOnEmptyHistory() throws Exception {
        DefaultHistory history = setupHistory();
        history.purge();

        String readLineResult;
        in.setIn(new ByteArrayInputStream(translate("^Sa").getBytes()));
        readLineResult = reader.readLine();
        assertEquals(null, readLineResult);
    }
}
