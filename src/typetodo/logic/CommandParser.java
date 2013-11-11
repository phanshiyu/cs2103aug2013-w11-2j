//@author: A0090941E
package typetodo.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import typetodo.exception.InvalidCommandException;
import typetodo.exception.InvalidDateTimeException;
import typetodo.exception.InvalidFieldNameException;
import typetodo.exception.InvalidFormatException;
import typetodo.exception.MissingFieldException;
import typetodo.exception.ReservedCharacterException;
import typetodo.model.FieldName;
import typetodo.model.TaskType;
import typetodo.sync.SyncController;

public class CommandParser {
	private Schedule schedule;
	private MainController sc;
	private SyncController syncController;
	private HelpController helpController;
	private CurrentTaskListManager taskListManager;

	private static final String MESSAGE_EXCEPTION_INVALID = "Invalid command, please refer to catalog by entering 'help'.";
	private static final String MESSAGE_EXCEPTION_MISSING_COLON = "Missing ';'";
	private static final String MESSAGE_EXCEPTION_MISSING_TITLE = "Title of task is missing, please refer to catalog by entering 'help'";
	private static final String MESSAGE_EXCEPTION_RESERVED_CHAR_DESCRIPTION = "';' is a reserved character and should not be found in the description";
	private static final String MESSAGE_EXCEPTION_RESERVED_CHAR_TITLE = "'+' is a reserved character and should not be found in the title";
	private static final String MESSAGE_EXCEPTION_MISSING_FIELDNAME = "Field Name is missing, please refer to catalog by entering 'help edit'";
	private static final String MESSAGE_EXCEPTION_MISSING_NEWVALUE = "New value is missing, please refer to catalog by entering 'help edit'";
	private static final String MESSAGE_EXCEPTION_DATETIME_FORMAT = "Please specify both date and time in all fields. Please use 'mm/dd' format if you want to type standard date.";
	private static final String MESSAGE_EXCEPTION_RESERVED_CHAR_1 = "'<' is a reserved character and cannot be used";
	private static final String MESSAGE_EXCEPTION_RESERVED_CHAR_2 = "'>' is a reserved character and cannot be used";
	private static final String MESSAGE_EXCEPTION_RESERVED_CHAR_3 = "'[' is a reserved character and cannot be used";
	private static final String MESSAGE_EXCEPTION_RESERVED_CHAR_4 = "']' is a reserved character and cannot be used";
	private static final String MESSAGE_EXCEPTION_INVALID_ADD = "INVALID FORMAT. Please refer to catalog by entering 'help add'";
	private static final String MESSAGE_EXCEPTION_INVALID_EDIT = "INVALID FORMAT. Please refer to catalog by entering 'help edit'";
	private static final String MESSAGE_EXCEPTION_INVALID_SEARCH = "INVALID FORMAT. Please refer to catalog by entering 'help search'";
	private static final String MESSAGE_EXCEPTION_INVALID_DISPLAY = "INVALID FORMAT. Please refer to catalog by entering 'help display'";
	private static final String MESSAGE_EXCEPTION_INVALID_DONE = "INVALID FORMAT. Please refer to catalog by entering 'help done'";

	public CommandParser(MainController sc, Schedule schedule,
			CurrentTaskListManager taskListManager,
			SyncController syncController, HelpController helpController) {
		this.sc = sc;
		this.schedule = schedule;
		this.taskListManager = taskListManager;
		this.syncController = syncController;
		this.helpController = helpController;
	}

	/**
	 * Extracts and returns the command from the user input.
	 * 
	 * @param userInput
	 *            A String containing the raw user input
	 * @return the command
	 * @throws InvalidCommandException
	 */
	private CommandType getCommand(String userInput)
			throws InvalidCommandException {

		Scanner scanner = new Scanner(userInput);
		// Extract the first string
		String command = scanner.next().toLowerCase();
		scanner.close();

		HashMap<CommandType, List<String>> commandSynonyms = new HashMap<CommandType, List<String>>();
		/** hard coded library of possible various user command inputs. */
		commandSynonyms.put(CommandType.ADD, Arrays.asList("add", "insert"));
		commandSynonyms.put(CommandType.DELETE,
				Arrays.asList("delete", "del", "de", "-", "remove"));
		commandSynonyms.put(CommandType.DONE, Arrays.asList("done", "finished",
				"finish", "completed", "complete"));
		commandSynonyms.put(CommandType.DISPLAY,
				Arrays.asList("display", "view", "show", "see", "list"));
		commandSynonyms.put(CommandType.HELP, Arrays.asList("help"));
		commandSynonyms.put(CommandType.HOME, Arrays.asList("home", "today"));
		commandSynonyms.put(CommandType.HOTKEY, Arrays.asList("hotkey",
				"quick", "hot key", "hotkeys", "hot", "short"));
		commandSynonyms.put(CommandType.UPDATE,
				Arrays.asList("update", "edit", "change"));
		commandSynonyms
				.put(CommandType.SEARCH, Arrays.asList("search", "find"));
		commandSynonyms.put(CommandType.UNDO, Arrays.asList("undo"));
		commandSynonyms.put(CommandType.EXIT, Arrays.asList("exit"));
		commandSynonyms.put(CommandType.SYNC, Arrays.asList("sync"));

		for (CommandType commandType : commandSynonyms.keySet()) {
			if (commandSynonyms.get(commandType).contains(command)) {
				return commandType;
			}
		}

		throw new InvalidCommandException(MESSAGE_EXCEPTION_INVALID);
	}

	private TaskType getTaskType(String userInput)
			throws InvalidCommandException {
		Scanner scanner = new Scanner(userInput);
		// Extract the taskType in view <task type>
		scanner.next();// throw command "view"
		String taskType = scanner.next().toLowerCase();
		scanner.close();

		HashMap<TaskType, List<String>> typeSynonyms = new HashMap<TaskType, List<String>>();
		/** hard coded library of possible various user command inputs. */
		typeSynonyms.put(TaskType.DEADLINE_TASK,
				Arrays.asList("deadline", "due"));
		typeSynonyms.put(TaskType.FLOATING_TASK,
				Arrays.asList("floating", "normal", "float"));
		typeSynonyms.put(TaskType.TIMED_TASK,
				Arrays.asList("timedtask", "timed", "slot"));

		for (TaskType type : typeSynonyms.keySet()) {
			if (typeSynonyms.get(type).contains(taskType)) {
				return type;
			}
		}

		throw new InvalidCommandException(MESSAGE_EXCEPTION_INVALID);
	}

	/**
	 * Extracts and returns the title from the user input.
	 * 
	 * @param userInput
	 *            A String containing the raw user input
	 * @return the title
	 * @throws InvalidFormatException
	 *             if user input does not have ';'
	 * @throws MissingFieldException
	 *             if user input does not contain a title
	 */
	private String getTitle(String userInput) throws InvalidFormatException,
			MissingFieldException, ReservedCharacterException {
		if (userInput.indexOf(';') == -1) {
			throw new InvalidFormatException(MESSAGE_EXCEPTION_MISSING_COLON);
		}

		Scanner scanner = new Scanner(userInput);
		scanner.next(); // throw user command;
		scanner.useDelimiter(";"); // title of task must always end with ";"
		String title = scanner.next().trim();
		scanner.close();

		if (title.equals("")) {
			throw new MissingFieldException(MESSAGE_EXCEPTION_MISSING_TITLE);
		} else if (title.contains("+")) {
			throw new ReservedCharacterException(
					MESSAGE_EXCEPTION_RESERVED_CHAR_TITLE);
		} else {
			assert title != "" && !title.contains("+") && !title.contains(";");
			return title.trim();
		}
	}

	/**
	 * Extracts and returns the description from the user input.
	 * 
	 * @param userInput
	 *            A String containing the raw user input
	 * @return the description
	 * @throws InvalidFormatException
	 *             if restricted char ';' is found in the description
	 */
	private String getDescription(String userInput)
			throws ReservedCharacterException {
		String description;
		int indexOfDescription = userInput.indexOf('+');

		// no description entered
		if (indexOfDescription == -1) {
			return "";
		}

		// description is empty
		else if (indexOfDescription == (userInput.length() - 1)) {
			return "";
		} else {
			description = userInput.substring(++indexOfDescription);
		}

		if (description.indexOf(';') != -1) {
			throw new ReservedCharacterException(
					MESSAGE_EXCEPTION_RESERVED_CHAR_DESCRIPTION);
		}

		return description.trim();
	}

	/**
	 * Extracts and returns the index from the user input.
	 * 
	 * @param userInput
	 *            A String containing the raw user input
	 * @return the index
	 */
	private int getIndex(String userInput) {
		Scanner scanner = new Scanner(userInput);
		scanner.next(); // throw the command
		int index = scanner.nextInt(); // next expected field is the index
		scanner.close();

		return index;
	}

	/**
	 * Extracts and returns the FieldName from the user input.
	 * 
	 * @param userInput
	 *            Raw user input
	 * @return the Field Name
	 * @throws InvalidFieldNameException
	 *             if field name is invalid
	 * @throws MissingFieldException
	 *             if field name is missing
	 */
	private FieldName getFieldName(String userInput)
			throws InvalidFieldNameException, MissingFieldException {
		FieldName fieldName;
		Scanner scanner = new Scanner(userInput);
		scanner.next(); // discard the command
		scanner.nextInt(); // discard the index

		try {
			fieldName = this.convertToFieldName(scanner.next());
		} catch (NoSuchElementException e) {
			scanner.close();
			throw new MissingFieldException(MESSAGE_EXCEPTION_MISSING_FIELDNAME);
		}
		scanner.close();

		return fieldName;
	}

	/**
	 * Extracts and returns the new value from the user input. It can be an
	 * instance of either a String, DateTime or Boolean, depending on the field
	 * that is to be updated.
	 * 
	 * @param userInput
	 *            Raw user input
	 * @return the new value
	 * @throws Exception
	 */
	private Object getNewValue(String userInput) throws Exception {
		Scanner scanner = new Scanner(userInput);
		scanner.next(); // throw away command
		scanner.nextInt(); // throw away index
		scanner.next(); // throw away fieldName

		String newValue;
		try {
			newValue = scanner.nextLine().trim();
		} catch (NoSuchElementException e) {
			scanner.close();
			throw new MissingFieldException(MESSAGE_EXCEPTION_MISSING_NEWVALUE);
		}
		scanner.close();

		switch (this.getFieldName(userInput)) {
		case TITLE:
		case DESCRIPTION:
			return newValue;
		case START:
		case END:
		case DEADLINE:
			return new DateTime(this.getDates(userInput).get(0));
		default:
			assert this.getFieldName(userInput) != null;
		}

		return null;
	}

	private String getKeyword(String userInput) {
		String keyword = null;
		Scanner scanner = new Scanner(userInput);

		scanner.next(); // throw away command
		keyword = scanner.nextLine();// get keyword
		scanner.close();

		return keyword.trim();
	}

	private ArrayList<DateTime> getDates(String userInput) throws Exception {
		String dateField;
		Scanner scanner = new Scanner(userInput);
		scanner.next();// throw away command

		if (userInput.contains(";")) {
			scanner.useDelimiter(";");
			scanner.next();
			scanner.useDelimiter("\\+");
			dateField = scanner.next().substring(1).trim();
		} else if (this.getCommand(userInput).equals(CommandType.DISPLAY)) {
			dateField = scanner.nextLine().trim();
		} else {
			scanner.next();// throw away index
			scanner.next();// throw away fieldName
			dateField = scanner.nextLine().trim();
		}

		scanner.close();

		dateField = modifyDate(dateField);

		List<java.util.Date> javaDates = new PrettyTimeParser()
				.parse(dateField);
		ArrayList<DateTime> jodaDates = new ArrayList<DateTime>();

		while (!javaDates.isEmpty()) {
			DateTime validDates = new DateTime(javaDates.remove(0));
			jodaDates.add(validDates);
		}

		if ((dateField.contains(" to ") && jodaDates.size() == 1)
				|| jodaDates.size() == 0) {
			throw new InvalidDateTimeException(
					MESSAGE_EXCEPTION_DATETIME_FORMAT);
		}

		return jodaDates;
	}

	// modify parsed-in date time input into a system-readable one
	private String modifyDate(String dateInput) {
		String result, startAmPm, endAmPm;
		result = dateInput.toLowerCase().replaceAll("-", " to ");
		result = result.toLowerCase().replaceAll("tmr", "tomorrow");

		int indexOfTo = result.indexOf(" to ");
		if (indexOfTo != -1) {
			endAmPm = result.substring(result.trim().length() - 2)
					.toLowerCase().trim();
			startAmPm = result.substring(indexOfTo - 2, indexOfTo).trim()
					.toLowerCase();

			if (endAmPm.equals("pm")
					&& (!startAmPm.equals("am") || !startAmPm.equals("pm"))) {
				startAmPm = startAmPm + "pm";
			}
			result = result.substring(0, indexOfTo - 2) + " " + startAmPm
					+ result.substring(indexOfTo);
		}
		return result;
	}

	private String getHelpType(String userInput) throws InvalidCommandException {
		String helpType;
		Scanner scanner = new Scanner(userInput);
		scanner.next();// throw away command;

		if (scanner.hasNext()) {
			helpType = scanner.next();
		} else {
			helpType = "";
		}
		scanner.close();

		return helpType;
	}

	/**
	 * convert from string to FieldName and return FieldName.
	 * 
	 * @throws InvalidFieldNameException
	 */
	private FieldName convertToFieldName(String fnString)
			throws InvalidFieldNameException {
		HashMap<FieldName, List<String>> fieldNameSynonyms = new HashMap<FieldName, List<String>>();
		fieldNameSynonyms.put(FieldName.TITLE, Arrays.asList("NAME", "TITLE"));
		fieldNameSynonyms.put(FieldName.DESCRIPTION,
				Arrays.asList("DESCRIPTION", "DESC"));
		fieldNameSynonyms.put(FieldName.START, Arrays.asList("START"));
		fieldNameSynonyms.put(FieldName.END, Arrays.asList("END"));
		fieldNameSynonyms.put(FieldName.DEADLINE, Arrays.asList("DEADLINE"));

		for (FieldName fieldName : fieldNameSynonyms.keySet()) {
			if (fieldNameSynonyms.get(fieldName).contains(fnString)) {
				return fieldName;
			}
		}

		throw new InvalidFieldNameException(
				"\""
						+ fnString
						+ "\" is not a valid Field Name, please refer to catalog by entering 'help edit'");
	}

	private boolean isViewAll(String userInput) {
		Scanner scanner = new Scanner(userInput);
		scanner.next(); // discard user command
		if (scanner.hasNext()) {
			if (scanner.next().equals("all")) {
				scanner.close();
				return true;
			}
		}
		scanner.close();

		return false;
	}

	private void checkForReservedCharacters(String userInput)
			throws ReservedCharacterException {
		if (userInput.indexOf("<") != -1) {
			throw new ReservedCharacterException(
					MESSAGE_EXCEPTION_RESERVED_CHAR_1);
		}
		if (userInput.indexOf(">") != -1) {
			throw new ReservedCharacterException(
					MESSAGE_EXCEPTION_RESERVED_CHAR_2);
		}
		if (userInput.indexOf("[") != -1) {
			throw new ReservedCharacterException(
					MESSAGE_EXCEPTION_RESERVED_CHAR_3);
		}
		if (userInput.indexOf("]") != -1) {
			throw new ReservedCharacterException(
					MESSAGE_EXCEPTION_RESERVED_CHAR_4);
		}
	}

	/**
	 * 
	 * @param userInput
	 * @return
	 * @throws Exception
	 */
	public Command parse(String userInput) throws Exception {
		this.checkForReservedCharacters(userInput);

		Command command = null;
		switch (this.getCommand(userInput)) {

		case ADD:
			String title = this.getTitle(userInput);
			String description = this.getDescription(userInput);
			ArrayList<DateTime> dates = this.getDates(userInput);

			if (dates.isEmpty()) {
				command = new CommandAddTask(schedule, title, description);
			} else if (dates.size() == 1) {
				DateTime deadline = dates.get(0);
				command = new CommandAddTask(schedule, title, description,
						deadline);
			} else if (dates.size() == 2) {
				DateTime start = dates.get(0);
				DateTime end = dates.get(1);

				command = new CommandAddTask(schedule, title, description, start, end);
			} else {
				throw new InvalidFormatException(MESSAGE_EXCEPTION_INVALID_ADD);
			}
			break;

		case DELETE:
			int index = this.getIndex(userInput);
			command = new CommandDeleteTask(schedule, index);
			break;

		case UPDATE:
			int taskId = this.getIndex(userInput);
			FieldName fieldName = this.getFieldName(userInput);
			Object newValue = this.getNewValue(userInput);

			if (newValue instanceof String) {
				command = new CommandEditTask(schedule, taskId, fieldName,
						(String) newValue);
			} else if (newValue instanceof DateTime) {
				command = new CommandEditTask(schedule, taskId, fieldName,
						(DateTime) newValue);
			} else {
				throw new InvalidFormatException(MESSAGE_EXCEPTION_INVALID_EDIT);
			}
			break;

		case SEARCH:
			try {
				String keyword = this.getKeyword(userInput);
				command = new CommandSearch(schedule, keyword);
			} catch (Exception e) {
				throw new InvalidFormatException(MESSAGE_EXCEPTION_INVALID_SEARCH);
			}
			break;

		case DISPLAY:
			DateTime dateTime;
			TaskType taskType;

			if (this.isViewAll(userInput)) {
				command = new CommandView(taskListManager);
				break;
			}
			try {
				taskType = getTaskType(userInput);
				command = new CommandView(taskListManager,taskType);
			} catch (Exception e) {
				try {
					dateTime = this.getDates(userInput).get(0);
					System.out.println("task date: " + dateTime);
					command = new CommandView(taskListManager, dateTime);
				} catch (Exception ex) {
					throw new InvalidFormatException(MESSAGE_EXCEPTION_INVALID_DISPLAY);
				}
			}

			break;

		case DONE:
			try {
				int indexOfCompletedTask = this.getIndex(userInput);
				command = new CommandCompleted(schedule, indexOfCompletedTask);
			} catch (Exception e) {
				throw new InvalidFormatException(MESSAGE_EXCEPTION_INVALID_DONE);
			}
			break;

		case HOME:
			command = new CommandHome(taskListManager);
			break;

		case UNDO:
			command = new CommandUndo(sc);
			break;

		case HELP:
			String helpType = getHelpType(userInput);
			if (helpType != "") {
				CommandType commandType = getCommand(helpType);
				helpController = new HelpController(commandType);
				command = new CommandHelp(helpController);
			} else {
				helpController = new HelpController(helpType);
				command = new CommandHelp(helpController);
			}
			break;

		case EXIT:
			command = new CommandExit();
			break;

		case SYNC:
			command = new CommandSync(syncController);
			break;

		default:
			// TODO:
			throw new Error();
		}
		
		return command;
	}
	
	private static Logger logger = Logger.getLogger("ParserLogger");

	public void logParser() {
		logger.log(Level.INFO, "going to start processing");
		try {
			//TODO
		} catch (Exception e) {
			logger.log(Level.WARNING, "process error", e);
			logger.log(Level.INFO, "end of processing");
		}
	}
}