package cz.cvut.kbss.ontodriver.jena.query;

import cz.cvut.kbss.ontodriver.jena.connector.StatementExecutor;
import cz.cvut.kbss.ontodriver.jena.environment.Generator;
import cz.cvut.kbss.ontodriver.util.StatementHolder;
import cz.cvut.kbss.ontodriver.util.Vocabulary;
import org.apache.jena.query.QueryFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JenaPreparedStatementTest {

    private static final String QUERY = "SELECT * WHERE { ?x ?y ?z . }";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private StatementExecutor executor;

    @Mock
    private AbstractResultSet resultSet;

    private JenaPreparedStatement statement;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(executor.executeSelectQuery(any())).thenReturn(resultSet);
        when(executor.executeAskQuery(any())).thenReturn(resultSet);
    }

    @Test
    public void constructorAnalyzesQueryPassedAsParameter() throws Exception {
        this.statement = new JenaPreparedStatement(executor, QUERY);
        final StatementHolder holder = getStatementHolder();
        assertNotNull(holder);
        assertEquals(QUERY, holder.getStatement());
    }

    private StatementHolder getStatementHolder() throws Exception {
        final Field holderField = JenaPreparedStatement.class.getDeclaredField("holder");
        holderField.setAccessible(true);
        return (StatementHolder) holderField.get(statement);
    }

    @Test
    public void constructorThrowsIllegalArgumentForEmptyQuery() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(containsString("Statement cannot be empty"));
        this.statement = new JenaPreparedStatement(executor, "");
    }

    @Test
    public void setParameterSetsParameterOnQuery() throws Exception {
        this.statement = new JenaPreparedStatement(executor, QUERY);
        final String value = "<" + Vocabulary.RDF_TYPE + ">";
        statement.setObject("y", value);
        final StatementHolder holder = getStatementHolder();
        assertThat(holder.assembleStatement(), containsString(value));
    }

    @Test
    public void setParameterThrowsIllegalStateOnClosedStatement() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(containsString("Statement is closed"));
        this.statement = new JenaPreparedStatement(executor, QUERY);
        statement.close();
        statement.setObject("y", "rdf:type");
    }

    @Test
    public void executeQueryExecutesStatementWithParameter() throws Exception {
        this.statement = new JenaPreparedStatement(executor, QUERY);
        final String value = "<" + Vocabulary.RDF_TYPE + ">";
        statement.setObject("y", value);
        final String expected = QUERY.replace("?y", value);
        statement.executeQuery();
        verify(executor).executeSelectQuery(QueryFactory.create(expected));
    }

    @Test
    public void executeQueryThrowsIllegalStateForClosedStatement() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(containsString("Statement is closed"));
        this.statement = new JenaPreparedStatement(executor, QUERY);
        statement.close();
        statement.executeQuery();
    }

    @Test
    public void executeUpdateExecutesUpdateStatement() throws Exception {
        final String update = "INSERT DATA { _:a1 a ?type . }";
        this.statement = new JenaPreparedStatement(executor, update);
        final String value = "<" + Generator.generateUri() + ">";
        statement.setObject("type", value);
        final String expected = update.replace("?type", value);
        statement.executeUpdate();
        verify(executor).executeUpdate(expected);
    }

    @Test
    public void executeUpdateThrowsIllegalStateForClosedStatement() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(containsString("Statement is closed"));
        final String update = "INSERT DATA { _:a1 a ?type . }";
        this.statement = new JenaPreparedStatement(executor, update);
        statement.close();
        statement.executeUpdate();
    }

    @Test
    public void clearParametersClearsAlreadySetParameters() throws Exception {
        this.statement = new JenaPreparedStatement(executor, QUERY);
        final String value = "<" + Vocabulary.RDF_TYPE + ">";
        statement.setObject("y", value);
        statement.clearParameters();
        statement.executeQuery();
        verify(executor).executeSelectQuery(QueryFactory.create(QUERY));
    }

    @Test
    public void clearParametersThrowsIllegalStateForClosedStatement() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(containsString("Statement is closed"));
        this.statement = new JenaPreparedStatement(executor, QUERY);
        final String value = "<" + Vocabulary.RDF_TYPE + ">";
        statement.setObject("y", value);
        statement.close();
        statement.clearParameters();
    }
}