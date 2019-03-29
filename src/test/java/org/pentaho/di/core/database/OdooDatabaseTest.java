/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Copyright 2017 West IT Solutions
 */

package org.pentaho.di.core.database;

import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;

public class OdooDatabaseTest {

  @BeforeClass
  public static void setUpOnce() throws KettleException {
    // Register custom DatabaseMeta class
    DatabasePluginType dbPluginType = (DatabasePluginType) PluginRegistry.getInstance().getPluginType( DatabasePluginType.class );
    dbPluginType.registerCustom( OdooDatabaseMeta.class, null, "OdooDatabaseMeta", "Odoo Server", null, null );

    KettleClientEnvironment.init();
  }

  @Test
  public void testReadDataIT() throws KettleDatabaseException, SQLException {
    OdooDatabaseMeta demoMeta = new OdooDatabaseMeta();
    demoMeta.setPluginId( "OdooDatabaseMeta" );
    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setDatabaseInterface( demoMeta );

    Database db = new Database( new LoggingObject( this ), dbMeta );
    assertNotNull(db);
  }
}
