package xyz.nickr.superchat.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import xyz.nickr.superchat.SuperChatController;
import xyz.nickr.superchat.sys.Group;
import xyz.nickr.superchat.sys.GroupConfiguration;
import xyz.nickr.superchat.sys.GroupType;
import xyz.nickr.superchat.sys.Message;
import xyz.nickr.superchat.sys.Sys;
import xyz.nickr.superchat.sys.User;

public class HelpCommand implements Command {

    @Override
    public String[] names() {
        return new String[] { "help" };
    }

    @Override
    public String[] help(User user, boolean userChat) {
        return new String[] { "(search)", "see the help menu, or only matching lines" };
    }

    @Override
    public boolean userchat() {
        return true;
    }

    @Override
    public boolean alwaysEnabled() {
        return true;
    }

    String getCmdHelp(Command cmd, User user, boolean userChat) {
        String pre = SuperChatController.COMMAND_PREFIX;
        String s = pre;
        for (String n : cmd.names()) {
            if (s.length() > pre.length())
                s += ",";
            s += n.trim();
        }
        String cmdHelp = cmd.help(user, userChat)[0];
        if (cmdHelp.length() > 0)
            s += " " + cmd.help(user, userChat)[0];
        return s;
    }

    String pad(String str, int len) {
        while (str.length() < len)
            str += " "; // + str;
        return str;
    }

    @Override
    public void exec(Sys sys, User user, Group conv, String used, String[] args, Message message) {
        List<Command> cmds = new ArrayList<>(SuperChatController.COMMANDS.size());
        SuperChatController.COMMANDS.forEach((name, cmd) -> {
            boolean go = true;
            for (Command c : cmds)
                if (c == cmd)
                    go = false;
            if (go)
                cmds.add(cmd);
        });
        GroupConfiguration cfg = SuperChatController.getGroupConfiguration(conv);
        if (cfg != null)
            cmds.removeIf(cmd -> !cfg.isCommandEnabled(cmd));
        else if (conv.getType() == GroupType.USER)
            cmds.removeIf(cmd -> !cmd.userchat());
        else
            cmds.removeIf(cmd -> !cmd.alwaysEnabled());
        if (cmds.isEmpty()) {
            conv.sendMessage("It looks like there are no commands enabled in this chat.");
            return;
        }
        AtomicInteger maxLen = new AtomicInteger(0);
        cmds.forEach(c -> {
            String cmdHelp = getCmdHelp(c, user, conv.getType() == GroupType.USER);
            if (c.perm() == Command.DEFAULT_PERMISSION && cmdHelp.length() > maxLen.get())
                maxLen.set(cmdHelp.length());
        });
        List<String> strings = new ArrayList<>(SuperChatController.COMMANDS.size());
        StringBuilder builder = new StringBuilder();
        cmds.forEach(c -> {
            String[] help = c.help(user, conv.getType() == GroupType.USER);
            if (c.perm() == Command.DEFAULT_PERMISSION)
                strings.add(pad(getCmdHelp(c, user, conv.getType() == GroupType.USER), maxLen.get()) + " - " + help[1]);
        });
        if (SuperChatController.HELP_IGNORE_WHITESPACE)
            strings.sort((s1, s2) -> s1.trim().compareTo(s2.trim()));
        else
            strings.sort(null);
        if (args.length > 0)
            strings.removeIf(s -> !s.contains(args[0]));
        String welcome = String.format(SuperChatController.WELCOME_MESSAGE, conv.getDisplayName());
        if (conv.getType() == GroupType.USER)
            welcome = "Welcome, " + user.getUsername();
        if (strings.isEmpty()) {
            conv.sendMessage(sys.message().bold(true).text(welcome));
            return;
        }
        int mid = welcome.length() / 2;
        String wel = pad(welcome.substring(0, mid), maxLen.get());
        String come = welcome.substring(mid);
        maxLen.set(0);
        strings.forEach(s -> {
            if (s.length() > maxLen.get())
                maxLen.set(s.length());
            builder.append("\n" + sys.message().text(s));
        });
        String spaces = SuperChatController.HELP_WELCOME_CENTRED ? strings.get(0).replaceAll("\\S.+", "") : wel.replaceAll("\\S+", "");
        conv.sendMessage(sys.message().code(true).text(spaces).code(false).bold(true).text(wel.trim() + come).bold(false).code(true).text(builder.toString()));
    }

}