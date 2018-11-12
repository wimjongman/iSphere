/*****************************************************************************
      SQLJEP - Java SQL Expression Parser 0.2
      November 1 2006
         (c) Copyright 2006, Alexey Gaidukov
      SQLJEP Author: Alexey Gaidukov

      SQLJEP is based on JEP 2.24 (http://www.singularsys.com/jep/)
           (c) Copyright 2002, Nathan Funk
 
      See LICENSE.txt for license information.
*****************************************************************************/

package org.medfoster.sqljep.function;

import java.sql.*;
import java.util.Calendar;

import static java.util.Calendar.*;
import org.medfoster.sqljep.*;

public class DayOfYear extends PostfixCommand {
	final public int getNumberOfParameters() {
		return 1;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		Comparable param = runtime.stack.pop();
		runtime.stack.push(dayOfYear(param, runtime.calendar));
	}

	public static Integer dayOfYear(Comparable param, Calendar cal) throws ParseException {
		if (param == null) {
			return null;
		}
		if (param instanceof Timestamp || param instanceof Date) {
			java.util.Date ts = (java.util.Date)param;
			cal.setTimeInMillis(ts.getTime());
			return new Integer(cal.get(DAY_OF_YEAR));
		}
		throw new ParseException(WRONG_TYPE+" weekofyear("+param.getClass()+")");
	}
}

