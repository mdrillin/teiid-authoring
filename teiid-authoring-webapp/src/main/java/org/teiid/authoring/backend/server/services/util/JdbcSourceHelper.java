package org.teiid.authoring.backend.server.services.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.teiid.authoring.share.exceptions.DataVirtUiException;

/**
 * Contains methods for finding JDBC sources on the server
 */
public class JdbcSourceHelper {

	private static JdbcSourceHelper instance = new JdbcSourceHelper();

	private static final String WRAPPER_DS = "org.jboss.resource.adapter.jdbc.WrapperDataSource"; //$NON-NLS-1$
	private static final String WRAPPER_DS_AS7 = "org.jboss.jca.adapters.jdbc.WrapperDataSource"; //$NON-NLS-1$
	private static final String TEIID_DRIVER_PREFIX = "teiid";
	private static final String JDBC_CONTEXT1 = "java:/"; //$NON-NLS-1$
	private static final String JDBC_CONTEXT2 = "java:/datasources/"; //$NON-NLS-1$
	private static final String JDBC_CONTEXT3 = "java:jboss/datasources/"; //$NON-NLS-1$
	private static List<String> JDBC_CONTEXTS = new ArrayList<String>() { {
		add(JDBC_CONTEXT1);
		add(JDBC_CONTEXT2);
		add(JDBC_CONTEXT3);
	}};
	private InitialContext context;
	
	/**
	 * Get the singleton instance
	 *
	 * @return instance
	 */
	public static JdbcSourceHelper getInstance() {
		return instance;
	}

	/*
	 * Create a VdbHelper
	 */
	private JdbcSourceHelper() {
	}

    /*
     * Get List of all available JDBC Source Names. The 'Datasource Names' are the jndi names of the
     * queryable jdbc sources on the server.
     * @param teiidOnly 'true' if only Teiid sources are to be returned, 'false' otherwise.
     * @return the list of datasource names
     */
    public List<String> getJdbcSourceNames(boolean teiidOnly) throws DataVirtUiException {
    	// Get DataSources Map
    	Map<String,DataSource> mDatasources = getDataSourceMap();

    	// Get DataSource names
    	List<String> resultList = new ArrayList<String>();

    	Set<String> dsNames = mDatasources.keySet();
    	Iterator<String> nameIter = dsNames.iterator();
    	while(nameIter.hasNext()) {
    		String dsName = nameIter.next();
    		if(dsName!=null && !dsName.startsWith("java:/PREVIEW_")) {
    			DataSource ds = mDatasources.get(dsName);
    			if(!teiidOnly) {
    				resultList.add(dsName);
    			} else if(isTeiidSource(ds)) {
    				resultList.add(dsName);
    			}
    		}
    	}
    	
    	return resultList;
    }
    
    /*
     * Determine if the data source is a Teiid source
     * @param dataSource the data source
     * @return 'true' if the source is a Teiid source
     */
    public boolean isTeiidSource(DataSource dataSource) {
    	boolean isVdb = false;
    	Connection conn = null;
    	if(dataSource!=null) {
    		try {
    			conn = dataSource.getConnection();
    			if(conn!=null) {
    				String driverName = conn.getMetaData().getDriverName();
    				if(driverName!=null && driverName.trim().toLowerCase().startsWith(TEIID_DRIVER_PREFIX)) {
    					isVdb = true;
    				}
    			}
    		} catch (SQLException e) {
    		} finally {
    			if(conn!=null) {
    				try {
    					conn.close();
    				} catch (SQLException e) {
    				}
    			}
    		}
    	}
    	return isVdb;
    }
    
    /*
     * Refresh the DataSource Maps
     */
    public Map<String,DataSource> getDataSourceMap( ) {
    	// Clear the DataSource Maps
    	Map<String,DataSource> mDatasources = new TreeMap<String,DataSource>();
    	Map<String,String> mDatasourceSchemas = new TreeMap<String,String>();

    	// New Context
    	if(context==null) {
    		try {
    			context = new InitialContext();
    		} catch (Exception e) {
    		}
    	}
    	
    	if(context==null) return mDatasources;

    	NamingEnumeration<javax.naming.NameClassPair> ne = null;
    	// Try the list of possible context names
    	for(String jdbcContext : JDBC_CONTEXTS) {
    		try {
    			Context theJdbcContext = (Context) context.lookup(jdbcContext);
    			ne = theJdbcContext.list("");
    		} catch (NamingException e1) {
    			System.out.println("Error with lookup");
    		}

    		while (ne!=null && ne.hasMoreElements()) {
    			javax.naming.NameClassPair o = (javax.naming.NameClassPair) ne.nextElement();
    			Object bindingObject = null;

    			try {
    				if (o.getClassName().equals(WRAPPER_DS) || o.getClassName().equals(WRAPPER_DS_AS7)) {
    					bindingObject = context.lookup(jdbcContext + o.getName());
    				}
    			} catch (NamingException e1) {
    				System.out.println("Error with lookup of "+o.getName());
    			}

    			if(bindingObject!=null && bindingObject instanceof DataSource && !o.getName().equalsIgnoreCase("ModeShapeDS")) {
    				// Put DataSource into datasource Map
    				String key = jdbcContext.concat(o.getName());
    				mDatasources.put(key, (DataSource)bindingObject);

    				// Put Schema into schema Map
    				String schema = null;
    				try {
    					schema = (String) context.lookup("java:comp/env/schema/" + key);
    				} catch (NamingException e) {

    				}
    				mDatasourceSchemas.put(key, schema);
    			}
    		}
    	}
    	
    	return mDatasources;
    }

}

