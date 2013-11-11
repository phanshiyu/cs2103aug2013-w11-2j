package typetodo.logic;

public class CommandUndo implements Command{
	private static final String MESSAGE_UNDO = "Undo is successful";
	private MainController sc;
	
	public CommandUndo(MainController sc) {
		this.sc = sc;
	}
	
	public String execute() throws Exception {
		sc.undo();
		String feedback = MESSAGE_UNDO;
		return feedback;
	}
}
