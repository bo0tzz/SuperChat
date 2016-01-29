package xyz.nickr.superchat.cmd.perm;

import xyz.nickr.superchat.SuperChatPermissions;
import xyz.nickr.superchat.cmd.Command;
import xyz.nickr.superchat.cmd.Permission;
import xyz.nickr.superchat.sys.Group;
import xyz.nickr.superchat.sys.Message;
import xyz.nickr.superchat.sys.MessageBuilder;
import xyz.nickr.superchat.sys.Sys;
import xyz.nickr.superchat.sys.User;

public class DelPermCommand implements Command {

    @Override
    public String[] names() {
        return new String[]{ "delperm" };
    }

    @Override
    public String[] help(User user, boolean userChat) {
        return new String[]{ "[username] [perm]", "removes [perm] from [username]" };
    }

    @Override
    public Permission perm() {
        return string("permissions.modify");
    }

    @Override
    public boolean userchat() {
        return true;
    }

    @Override
    public void exec(Sys sys, User user, Group conv, String used, String[] args, Message message) {
        if (args.length < 2) {
            sendUsage(null, user, conv);
        } else {
            MessageBuilder<?> mb = sys.message().bold(true).text(args[0]).bold(false);
            if (SuperChatPermissions.set(args[0], args[1], false)) {
                mb.text(" no longer has: ");
            } else {
                mb.text(" doesn't have: ");
            }
            conv.sendMessage(mb.bold(true).text(args[1]).bold(false));
        }
    }

}