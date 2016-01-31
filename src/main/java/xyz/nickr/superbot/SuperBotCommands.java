
package xyz.nickr.superbot;

import java.util.HashMap;
import java.util.Map;

import xyz.nickr.superbot.cmd.Command;
import xyz.nickr.superbot.cmd.HelpCommand;
import xyz.nickr.superbot.cmd.ReloadCommand;
import xyz.nickr.superbot.cmd.StopCommand;
import xyz.nickr.superbot.cmd.cfg.EditConfigCommand;
import xyz.nickr.superbot.cmd.cfg.ShowConfigCommand;
import xyz.nickr.superbot.cmd.fun.DefineCommand;
import xyz.nickr.superbot.cmd.fun.HangmanCommand;
import xyz.nickr.superbot.cmd.fun.NumberwangCommand;
import xyz.nickr.superbot.cmd.fun.TypeOutCommand;
import xyz.nickr.superbot.cmd.perm.AddPermCommand;
import xyz.nickr.superbot.cmd.perm.DelPermCommand;
import xyz.nickr.superbot.cmd.perm.ListPermsCommand;
import xyz.nickr.superbot.cmd.profile.CreateProfileCommand;
import xyz.nickr.superbot.cmd.profile.GetProfileCommand;
import xyz.nickr.superbot.cmd.shows.AddShowCommand;
import xyz.nickr.superbot.cmd.shows.ProgressCommand;
import xyz.nickr.superbot.cmd.shows.RemoveShowCommand;
import xyz.nickr.superbot.cmd.shows.SetProgressCommand;
import xyz.nickr.superbot.cmd.shows.ShowsCommand;
import xyz.nickr.superbot.cmd.shows.TimetableCommand;
import xyz.nickr.superbot.cmd.shows.ViewingOrderCommand;
import xyz.nickr.superbot.cmd.shows.WhoCommand;
import xyz.nickr.superbot.cmd.shows.WipeCommand;
import xyz.nickr.superbot.cmd.util.ConvertCommand;
import xyz.nickr.superbot.cmd.util.CurrencyCommand;
import xyz.nickr.superbot.cmd.util.GitCommand;
import xyz.nickr.superbot.cmd.util.JenkinsCommand;
import xyz.nickr.superbot.cmd.util.UidCommand;
import xyz.nickr.superbot.cmd.util.VersionCommand;
import xyz.nickr.superbot.sys.Group;
import xyz.nickr.superbot.sys.GroupConfiguration;
import xyz.nickr.superbot.sys.GroupType;
import xyz.nickr.superbot.sys.Message;
import xyz.nickr.superbot.sys.MessageBuilder;
import xyz.nickr.superbot.sys.Sys;
import xyz.nickr.superbot.sys.User;

public class SuperBotCommands {

    public static final Map<String, Command> COMMANDS = new HashMap<>();
    public static final String COMMAND_PREFIX = "+";

    public static void register(Command cmd) {
        for (String name : cmd.names())
            COMMANDS.put(name, cmd);
        cmd.init();
    }

    public static void loadCommands() {
        COMMANDS.clear();

        register(new HelpCommand());
        register(new ReloadCommand());
        register(new StopCommand());

        register(new EditConfigCommand());
        register(new ShowConfigCommand());

        register(new AddPermCommand());
        register(new DelPermCommand());
        register(new ListPermsCommand());

        register(new CreateProfileCommand());
        register(new GetProfileCommand());

        register(new AddShowCommand());
        register(new ProgressCommand());
        register(new RemoveShowCommand());
        register(new SetProgressCommand());
        register(new ShowsCommand());
        register(new TimetableCommand());
        register(new ViewingOrderCommand());
        register(new WhoCommand());
        register(new WipeCommand());

        register(new HangmanCommand());
        register(new NumberwangCommand());
        register(new TypeOutCommand());
        register(new DefineCommand());

        register(new ConvertCommand());
        register(new CurrencyCommand());
        register(new UidCommand());
        register(new GitCommand());
        register(new JenkinsCommand());
        register(new VersionCommand());
    }

    public static void exec(Sys sys, Group g, User u, Message message) {
        String msg = message.getMessage().trim();
        String[] words = msg.split("\\s+");

        if (msg.isEmpty() || words.length == 0 || !words[0].startsWith(COMMAND_PREFIX)) {
            return;
        }

        String cmdName = words[0].substring(COMMAND_PREFIX.length()).toLowerCase();
        Command cmd = COMMANDS.get(cmdName);
        if (cmd == null)
            return;

        GroupConfiguration cfg = SuperBotController.getGroupConfiguration(g);
        if (g.getType() == GroupType.GROUP) {
            if (cfg.isDisabled() || !cfg.isCommandEnabled(cmd))
                return;
        } else if (!cmd.userchat())
            return;

        String[] args = new String[words.length - 1];
        System.arraycopy(words, 1, args, 0, args.length);

        boolean userchat = g.getType() == GroupType.USER && cmd.userchat();

        if (g.getType() == GroupType.GROUP || userchat) {
            if (!cmd.perm().has(sys, g, u, u.getProfile())) {
                MessageBuilder<?> mb = sys.message();
                mb.bold(true).text("Error: ").bold(false);
                mb.text("You don't have permission to use " + COMMAND_PREFIX + cmdName + "!");
                g.sendMessage(mb.toString());
            } else {
                cmd.exec(sys, u, g, cmdName, args, message);
            }
        }
    }

}