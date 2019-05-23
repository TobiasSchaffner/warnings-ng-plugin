package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for a table that shows the issues of a build.
 *
 * @author Ullrich Hafner
 */
public class IssuesTable extends PageObject {
    private final String title;
    private final List<IssueRow> rows = new ArrayList<>();
    private final List<String> columnNames;

    /**
     * Creates a new instance of {@link IssuesTable}.
     *
     * @param page
     *         the whole details HTML page
     */
    @SuppressFBWarnings("BC")
    public IssuesTable(final HtmlPage page) {
        super(page);

        HtmlAnchor content = page.getAnchorByHref("#issuesContent");
        title = content.getTextContent();

        DomElement issues = page.getElementById("issues");
        assertThat(issues).isInstanceOf(HtmlTable.class);

        HtmlTable table = (HtmlTable) issues;
        List<HtmlTableRow> tableHeaderRows = table.getHeader().getRows();
        assertThat(tableHeaderRows).hasSize(1);

        HtmlTableRow header = tableHeaderRows.get(0);
        columnNames = getHeaders(header.getCells());

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);

        HtmlTableBody mainBody = bodies.get(0);
        waitForAjaxCall(mainBody);
        List<HtmlTableRow> contentRows = mainBody.getRows();

        for (HtmlTableRow row : contentRows) {
            List<HtmlTableCell> rowCells = row.getCells();
            rows.add(new IssueRow(rowCells, columnNames));
        }
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    @SuppressWarnings("PMD.SystemPrintln")
    static void waitForAjaxCall(final HtmlTableBody body) {
        while ("Loading - please wait ...".equals(
                body.getRows().get(0).getCells().get(0).getFirstChild().getTextContent())) {
            System.out.println("Waiting for Ajax call to populate issues table ...");
            body.getPage().getEnclosingWindow().getJobManager().waitForJobs(1000);
        }
    }

    private List<String> getHeaders(final List<HtmlTableCell> cells) {
        return cells.stream().map(HtmlTableCell::getTextContent).collect(Collectors.toList());
    }

    /**
     * Returns the title of the corresponding navigation bar (tab header).
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the table rows.
     *
     * @return the rows
     */
    public List<IssueRow> getRows() {
        return rows;
    }

    /**
     * Returns the row with the specified index.
     *
     * @param index
     *         index of the row
     *
     * @return the row
     */
    public IssueRow getRow(final int index) {
        return rows.get(index);
    }
}
