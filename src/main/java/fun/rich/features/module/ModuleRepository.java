package fun.rich.features.module;

import fun.rich.features.impl.combat.*;
import fun.rich.features.impl.misc.*;
import fun.rich.features.impl.movement.*;
import fun.rich.features.impl.player.*;
import fun.rich.features.impl.render.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleRepository {
    List<Module> modules = new ArrayList<>();

    public void setup() {
        register(
                new AntiAFK(),
                new JumpCircle(),
                new BetterMinecraft(),
                new ProjectileHelper(),
                new TargetStrafe(),
                new Strafe(),
                new AutoPilot(),
                new AirStuck(),
                new NoEntityTrace(),
                new NoFallDamage(),
                new ElytraMotion(),
                new LongJump(),
                new ShiftTap(),
                new AspectRatio(),
                new FreeLook(),
                new ClickPearl(),
//                new HitEffect(),
                new ClickFriend(),
                new TabParser(),
                new WindJump(),
                new TargetESP(),
                new NoWeb(),
                new ServerHelper(),
                new WaterSpeed(),
                new ItemScroller(),
                new Hud(),
                new AuctionHelper(),
                new ProjectilePrediction(),
                new WorldParticles(),
                new IRC(),
                new ElytraTarget(),
                new XRay(),
                new TriggerBot(),
                new Aura(),
                new AutoSwap(),
                new NoFriendDamage(),
                new HitBoxModule(),
                new AntiBot(),
                new AutoCrystal(),
                new AutoSprint(),
                new NoPush(),
                new ElytraHelper(),
                new JoinerHelper(),
                new NoDelay(),
                new Velocity(),
                new AutoRespawn(),
                new NoSlow(),
                new InventoryMove(),
                new Blink(),
                new AutoTool(),
                new Fly(),
                new FastBreak(),
                new CameraSettings(),
                new Speed(),
                new SwingAnimation(),
                new ViewModel(),
                new BlockOverlay(),
                new Jesus(),
                new Esp(),
                new BlockESP(),
                new AutoTotem(),
                new FreeCam(),
                new ChestStealer(),
                new AutoTpAccept(),
                new Arrows(),
                new AutoLeave(),
                new WorldTweaks(),
                new NoClip(),
                new NoRender(),
                new AutoBuy(),
                new NameProtect(),
                new SelfDestruct(),
                new SeeInvisible(),
                new TargetPearl(),
                new AutoArmor(),
                new AutoUse(),
                new NoInteract(),
                new CrossHair(),
                new SuperFireWork(),
                new Spider(),
                new ServerRPSpoofer(),
                new KillEffect()
        );
    }

    
    public void register(Module... module) {
        modules.addAll(List.of(module));
    }

    public List<Module> modules() {
        return modules;
    }
}
