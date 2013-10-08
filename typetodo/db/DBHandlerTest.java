/**
 * 
 */
package typetodo.db;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import typetodo.logic.DeadlineTask;
import typetodo.logic.FloatingTask;

/**
 * @author DennZ
 * 
 */
public class DBHandlerTest {

	@Test
	public void test() {
		DBHandler db;
		try {
			FloatingTask floatingTask = new FloatingTask("Name", "Desc");
			Date date = new Date();
			DeadlineTask deadlineTask = new DeadlineTask(1, "Name", "Desc", date);
			db = new DBHandler();
			assertEquals("Delete", true, (db.deleteTask(1)));
			assertEquals("Add task", 1, db.addTask(floatingTask));
			// assertEquals("Update task", true, db.updateTask(deadlineTask));
			assertEquals("Retreive", date,
					((DeadlineTask) db.retrieveList(date).get(0)).getDeadline());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
