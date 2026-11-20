package emmy.eminentmagic.barrier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class BarrierSavedData extends SavedData {
	private static final Codec<BarrierSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Barrier.CODEC.listOf().fieldOf("barriers").forGetter(data -> data.barriers())
	).apply(instance, BarrierSavedData::new));
	private static final SavedDataType<BarrierSavedData> TYPE =
			new SavedDataType<>("eminent_magic_barriers", BarrierSavedData::new, CODEC);

	private final Map<UUID, Barrier> barriers = new LinkedHashMap<>();

	private BarrierSavedData() {
	}

	private BarrierSavedData(List<Barrier> loadedBarriers) {
		for (Barrier barrier : loadedBarriers) {
			barriers.put(barrier.id(), barrier);
		}
	}

	public static BarrierSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	public List<Barrier> barriers() {
		return List.copyOf(barriers.values());
	}

	public Barrier get(UUID id) {
		return barriers.get(id);
	}

	public void put(Barrier barrier) {
		barriers.put(barrier.id(), barrier);
		setDirty();
	}

	public boolean remove(UUID id) {
		if (barriers.remove(id) == null) {
			return false;
		}
		setDirty();
		return true;
	}

	public int clear() {
		int removed = barriers.size();
		if (removed > 0) {
			barriers.clear();
			setDirty();
		}
		return removed;
	}

}
