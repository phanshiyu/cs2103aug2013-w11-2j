package typetodo.logic;

public class CommandHelp implements Command {
	private Schedule sc;
	private HelpController hc;

	public CommandHelp(Schedule sc, HelpController hc) {
		this.sc = sc;
		this.hc = hc;
	}

	public CommandHelp(Schedule sc) {
		this.sc = sc;
	}

	@Override
	public String execute() throws Exception {
		String feedback;
		sc.help();
		feedback=hc.getFeedback();
		return feedback;
	}
}
