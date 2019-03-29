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
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 *   Copyright 2017 West IT Solutions
 */

package org.pentaho.di.trans.steps.odoo.objectdelete;

import java.util.ArrayList;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.OdooDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.odoo.core.OdooHelper;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class OdooObjectDeleteData extends BaseStepData implements StepDataInterface {

  public OdooHelper helper;
  public ArrayList<Object> batchRows = new ArrayList<Object>();

  public OdooObjectDeleteData( DatabaseMeta databaseMeta ) throws KettleException {
    super();
    if ( !( databaseMeta.getDatabaseInterface() instanceof OdooDatabaseMeta ) ) {
      throw new KettleException( "A connection of type ODOO is expected" );
    }
    helper = new OdooHelper( databaseMeta );
  }
}
