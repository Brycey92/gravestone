package de.maxhenkel.gravestone.commands;

import com.google.common.collect.Lists;
import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.gravestone.Main;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RestoreCommand extends CommandBase {

    private final List<String> aliases = Lists.newArrayList("graverestore", "restoregrave");

    @Override
    @Nonnull
    public String getName()
    {
        return "graverestore";
    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "message.gravestone.restore.usage";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args ) throws CommandException
    {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(new TextComponentTranslation(getUsage(sender)));
        }

        UUID deathID = UUID.fromString(args[1]);
        EntityPlayerMP player;
        if(args.length == 3) {
            player = server.getPlayerList().getPlayerByUsername(args[2]);
            if(player == null) {
                throw new CommandException(new TextComponentTranslation("message.gravestone.nosuchplayer").getFormattedText()));
            }
        } else {
            Entity entity = sender.getCommandSenderEntity();

            if(!(entity instanceof EntityPlayerMP)) {
                throw new CommandException(new TextComponentTranslation("message.gravestone.run_as_player_or_give_target").getFormattedText());
            }

            player = (EntityPlayerMP) entity;
        }
        Death death = DeathManager.getDeath(player.getEntityWorld(), deathID);
        sender.sendMessage(new TextComponentTranslation("message.gravestone.death_id_not_found", deathID.toString()));

        if("add".equals(args[0])) {
            for (ItemStack stack : death.getAllItems()) {
                if (!player.inventory.addItemStackToInventory(stack)) {
                    player.dropItem(stack, false);
                }
            }
        }
        else if("replace".equals(args[0])) {
            player.inventory.clear();
            NonNullList<ItemStack> itemStacks = Main.GRAVESTONE.fillPlayerInventory(player, death);
            for (ItemStack stack : itemStacks) {
                player.dropItem(stack, false);
            }
        }

        sender.sendMessage(new TextComponentTranslation("message.gravestone.restore.success", player.getDisplayName()));
    }
}
