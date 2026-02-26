package net.alekrus.shphysarum.SculkPlayerAbility.SFX;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.core.ModBlocks.BlockTags;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.SoundUtil;
import com.github.sculkhorde.util.TickUnits;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.server.ServerLifecycleHooks;

public class AmbientSFXSystem {
    ArrayList<ServerPlayer> players;
    protected long lastTimeOfExecution = 0L;
    protected int populationRecountInterval = TickUnits.convertSecondsToTicks(10);

    public void serverTick() {
        long currentTime = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();
        if (Math.abs(currentTime - this.lastTimeOfExecution) >= (long)this.populationRecountInterval) {
            this.lastTimeOfExecution = currentTime;
            this.playAmbientSounds();
        }
    }

    public void playAmbientSounds() {
        this.players = new ArrayList<>(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers());
        Collections.shuffle(this.players);

        for(ServerPlayer player : this.players) {

            
            
            
            

            boolean isPlayerInfected = player.getCapability(net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider.SCULK_MIND)
                    .map(cap -> cap.hasSkill("root")) 
                    .orElse(false);

            if (isPlayerInfected) {
                continue; 
            }
            

            List<BlockInfo> blocks = this.getSurroundingBlockStatesAndPositions(player, 20);
            Optional<BlockPos> temp = getSoulitePos(blocks);
            if (temp.isPresent()) {
                SoundUtil.playAmbientSoundInLevel(player.level(), (BlockPos)temp.get(), (SoundEvent)ModSounds.SOULITE_AMBIENCE.get());
                PlayerProfileHandler.setTimeUntilNextAmbientSound(player, (long)TickUnits.convertSecondsToTicks(60));
                PlayerProfileHandler.setTimeOfLastAmbientSound(player, player.level().getGameTime());
                return;
            }

            if (PlayerProfileHandler.isTimeForNextAmbientSound(player, player.level().getGameTime())) {
                temp = getInfestedBlocksPos(blocks);
                if (temp.isPresent() && !PlayerProfileHandler.isPlayerActiveVessel(player)) {
                    SoundUtil.playAmbientSoundInLevel(player.level(), (BlockPos)temp.get(), (SoundEvent)ModSounds.INFESTATION_AMBIENCE.get());
                    PlayerProfileHandler.setTimeUntilNextAmbientSound(player, (long)TickUnits.convertSecondsToTicks(120));
                    PlayerProfileHandler.setTimeOfLastAmbientSound(player, player.level().getGameTime());
                    return;
                }
            }
        }
    }

    
    public List<BlockInfo> getSurroundingBlockStatesAndPositions(ServerPlayer player, int range) {
        Level level = player.level();
        Vec3 playerPos = player.position();
        List<BlockInfo> blocks = new ArrayList<>();
        Vec3[] directions = new Vec3[]{new Vec3(0.0F, 0.0F, -1.0F), new Vec3(0.0F, 0.0F, 1.0F), new Vec3(-1.0F, 0.0F, 0.0F), new Vec3(1.0F, 0.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, -1.0F, 0.0F), new Vec3(-1.0F, 0.0F, -1.0F), new Vec3(1.0F, 0.0F, -1.0F), new Vec3(-1.0F, 0.0F, 1.0F), new Vec3(1.0F, 0.0F, 1.0F), new Vec3(0.0F, 1.0F, -1.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(0.0F, -1.0F, -1.0F), new Vec3(0.0F, -1.0F, 1.0F)};

        for(Vec3 dir : directions) {
            Vec3 endPos = playerPos.add(dir.scale((double)range));
            BlockHitResult result = level.clip(new ClipContext(playerPos, endPos, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, player));
            if (result.getType() != Type.MISS) {
                blocks.add(new BlockInfo(result.getBlockPos(), level.getBlockState(result.getBlockPos())));
            }
        }
        Collections.shuffle(blocks);
        return blocks;
    }

    protected static Optional<BlockPos> getSoulitePos(List<BlockInfo> list) {
        Optional<BlockPos> pos = Optional.empty();
        for(int index = 0; index < list.size(); ++index) {
            if (containsSoulite((BlockInfo)list.get(index))) {
                pos = Optional.of(((BlockInfo)list.get(index)).pos);
            }
        }
        return pos;
    }

    protected static boolean containsSoulite(BlockInfo blockInfo) {
        return blockInfo.contains((Block)ModBlocks.SOULITE_BLOCK.get()) || blockInfo.contains((Block)ModBlocks.SOULITE_BUD_BLOCK.get()) || blockInfo.contains((Block)ModBlocks.SOULITE_CLUSTER_BLOCK.get()) || blockInfo.contains((Block)ModBlocks.DEPLETED_SOULITE_BLOCK.get()) || blockInfo.contains((Block)ModBlocks.BUDDING_SOULITE_BLOCK.get());
    }

    protected static Optional<BlockPos> getInfestedBlocksPos(List<BlockInfo> list) {
        Optional<BlockPos> pos = Optional.empty();
        for(int index = 0; index < list.size(); ++index) {
            if (containsInfestedBlocks((BlockInfo)list.get(index))) {
                pos = Optional.of(((BlockInfo)list.get(index)).pos);
            }
        }
        return pos;
    }

    protected static boolean containsInfestedBlocks(BlockInfo blockInfo) {
        return blockInfo.contains(BlockTags.INFESTED_BLOCK);
    }

    protected static class BlockInfo {
        public BlockPos pos;
        public BlockState state;

        BlockInfo(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        public boolean contains(Block block) {
            return this.state.is(block);
        }

        public boolean contains(TagKey<Block> tag) {
            return this.state.is(tag);
        }
    }
}