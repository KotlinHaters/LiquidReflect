package net.ccbluex.liquidbounce.features.module.modules.combat;


import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.ccbluex.liquidbounce.event.CallableEvent;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.events.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.value.IntegerValue;

/**
 * LiquidBounce Hacked Client
 * A minecraft forge injection client using Mixin
 *
 * @author CCBlueX
 * @game Minecraft
 */
@ModuleInfo(name = "FastBow", description = "Turns your bow into a machine gun.", category = ModuleCategory.COMBAT)
public class FastBow extends Module {
    private final IntegerValue packetsValue = new IntegerValue("Packets", 20, 3, 20);

    @EventTarget
    @SuppressWarnings("unused")
    private final CallableEvent<UpdateEvent> onEvent = event -> {
        if (!mc.thePlayer.isUsingItem()) {
            return;
        }

        if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBow) {
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.getCurrentEquippedItem(), 0F, 0F, 0F));

            float yaw = RotationUtils.targetRotation != null ? RotationUtils.targetRotation.getYaw() : mc.thePlayer.rotationYaw;
            float pitch = RotationUtils.targetRotation != null ? RotationUtils.targetRotation.getPitch() : mc.thePlayer.rotationPitch;

            for (int i = 0; i < packetsValue.get(); i++) {
                mc.getNetHandler().addToSendQueue(new C05PacketPlayerLook(yaw, pitch, true));
            }

            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            mc.thePlayer.setItemInUse(mc.thePlayer.inventory.getCurrentItem(), mc.thePlayer.inventory.getCurrentItem().getMaxItemUseDuration() - 1);
        }
    };
}
