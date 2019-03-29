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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OdooDatabaseMetaTest {

  /**
   * Test to see that the class is in the class path and returns the correct database factory
   */
  @Test
  public void testDatabaseFactoryClass(){
    OdooDatabaseMeta dbMeta = new OdooDatabaseMeta();
    assertEquals( "org.pentaho.di.odoo.core.OdooHelper", dbMeta.getDatabaseFactoryName() );
  }
}
