package org.egordorichev.lasttry.world;

import org.egordorichev.lasttry.LastTry;
import org.egordorichev.lasttry.entity.Drop;
import org.egordorichev.lasttry.entity.DroppedItem;
import org.egordorichev.lasttry.entity.Enemy;
import org.egordorichev.lasttry.entity.Entity;
import org.egordorichev.lasttry.item.ItemID;
import org.egordorichev.lasttry.item.tiles.Block;
import org.egordorichev.lasttry.item.tiles.Wall;
import org.egordorichev.lasttry.util.Rectangle;
import org.egordorichev.lasttry.world.biome.Biome;
import org.egordorichev.lasttry.world.tile.TileData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class World {
	/**
	 * Current world version generated by the game. Future versions may
	 * increment this number.
	 */
	public static final int CURRENT_VERSION = 2;
	/**
	 * Current biome
	 */
	public Biome currentBiome;
	/**
	 * World width in tiles.
	 */
	private final int width;
	/**
	 * World height in tiles.
	 */
	private final int height;
	/**
	 * World version.
	 */
	private int version = World.CURRENT_VERSION;
	/**
	 * World name.
	 */
	private String name;
	/**
	 * Array of tile data. 2D coordinates are encoded by:
	 * 
	 * <pre>
	 * index = x + y * world - width
	 * </pre>
	 */
	private TileData[] tiles;
	/**
	 * List of entities in the world.
	 */
	private List<Entity> entities = new ArrayList<>();;
	/**
	 * List of entities to be deleted in the next tick.
	 */
	private List<Entity> deadEntities = new ArrayList<>();
	/**
	 * The type of terrain generated.
	 * {@link org.egordorichev.lasttry.world.World.EvilType EvilType}.
	 */
	private EvilType evilType;
	/**
	 * Value indicating if the world has entered expert-mode.
	 */
	private boolean expert;

	public World(String name, int width, int height, TileData[] tiles) {
		this(name, width, height, LastTry.random.nextBoolean() ? EvilType.CORRUPTION : EvilType.CRIMSON, tiles);
	}

	public World(String name, int width, int height, EvilType evilType, TileData[] tiles) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.evilType = evilType;
		this.tiles = tiles;

		Biome.preload();
	}

	/**
	 * Render the world.
	 */
	public void render() {
		int windowWidth = LastTry.getWindowWidth();
		int windowHeight = LastTry.getWindowHeight();
		int tww = windowWidth / Block.TEX_SIZE;
		int twh = windowHeight / Block.TEX_SIZE;
		int tcx = (int) LastTry.camera.getX() / Block.TEX_SIZE;
		int tcy = (int) LastTry.camera.getY() / Block.TEX_SIZE;

		int minY = Math.max(0, tcy - 2);
		int maxY = Math.min(this.height - 1, tcy + twh + 2);
		int minX = Math.max(0, tcx - 2);
		int maxX = Math.min(this.width - 1, tcx + tww + 2);

		// Iterate coordinates, exclude ones not visible to the camera
		for (int y = minY; y < maxY; y++) {
			for (int x = minX; x < maxX; x++) {
				TileData tileData = this.getTile(x, y);
				tileData.render(x, y);
			}
		}

		// Render entities, exclude ones not visible to the camera
		for (Entity entity : this.entities) {
			int gx = entity.getGridX();
			int gy = entity.getGridY();
			int w = entity.getGridWidth();
			int h = entity.getGridHeight();
			if ((gx > minX - w && gx < maxX + w) && (gy > minY - h && gy < maxY + h)) {
				entity.render();
			}
		}
	}

	/**
	 * Update the world.
	 * 
	 * @param dt
	 *            The milliseconds passed since the last update.
	 */
	public void update(int dt) {
		// Remove dead entities
		for (Entity entity : this.deadEntities) {
			this.entities.remove(entity);
		}
		this.deadEntities.clear();
		// Update alive entities.
		for (Entity entity : this.entities) {
			entity.update(dt);
		}
	}

	/**
	 * Spawn an enemy in the world <i>(Type dictated by the id)</i>
	 * 
	 * @param id
	 *            Id of the type of entity to spawn.
	 * @param x
	 *            X-position to spawn entity at.
	 * @param y
	 *            Y-position to spawn entity at.
	 * @return
	 */
	public Enemy spawnEnemy(int id, int x, int y) {
		Enemy enemy = Enemy.create(id);

		if (enemy != null) {
			enemy.spawn(x, y);
			this.entities.add(enemy);
		}

		return enemy;
	}

	/**
	 * Spawn a dropped item in the world.
	 * 
	 * @param drop
	 *            Drop data.
	 * @param x
	 *            X-position to spawn dropped item at.
	 * @param y
	 *            Y-position to spawn dropped item at.
	 */
	public void spawnDrop(Drop drop, float x, float y) {
		DroppedItem droppedItem = new DroppedItem(drop.createHolder());
		droppedItem.spawn(x / Block.TEX_SIZE, y / Block.TEX_SIZE);

		this.entities.add(droppedItem);
		int vel = 10;

		droppedItem.setVelocity((LastTry.random.nextFloat() * (vel * 2)) - vel, -3);
	}

	/**
	 * Add an entity to the removal list. They will be removed the next tick.
	 * TODO: blood and gore
	 * 
	 * @param entity
	 *            Entity to remove.
	 */
	public void remove(Entity entity) {
		this.deadEntities.add(entity);
	}

	/**
	 * Check if the given bounds collide with blocks in the world.
	 * 
	 * @param bounds
	 *            Bounds to check collision for.
	 * @return If bounds collide with world's blocks.
	 */
	public boolean isColliding(Rectangle bounds) {
		Rectangle gridBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);

		gridBounds.x /= Block.TEX_SIZE;
		gridBounds.y /= Block.TEX_SIZE;
		gridBounds.width /= Block.TEX_SIZE;
		gridBounds.height /= Block.TEX_SIZE;

		for (int y = (int) gridBounds.y - 1; y < gridBounds.y + gridBounds.height + 1; y++) {
			for (int x = (int) gridBounds.x - 1; x < gridBounds.x + gridBounds.width + 1; x++) {
				if (!this.isInside(x, y)) {
					return true;
				}

				TileData data = this.getTile(x, y);

				if (data.block == null || !data.block.isSolid()) {
					continue;
				}

				Rectangle blockRect = new Rectangle(x * Block.TEX_SIZE, y * Block.TEX_SIZE, Block.TEX_SIZE,
						Block.TEX_SIZE);

				if (blockRect.intersects(bounds)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Check if the given position resides within the world's bounds.
	 * 
	 * @param x
	 *            X-position to check.
	 * @param y
	 *            Y-position to check.
	 * @return Position is inside world.
	 */
	public boolean isInside(int x, int y) {
		return (x >= 0 && x < this.width && y >= 0 && y < this.height);
	}

	/**
	 * Find the highest tile in the world at the given x-position.
	 * 
	 * @param x
	 *            X-position to check.
	 * @return Highest point that is clear.
	 */
	public int getHighest(int x) {
		int y = 0;
		while (true) {
			TileData td = getTile(x, y);
			if (td.block != null)
				return y - 3;
			y++;
		}
	}

	/**
	 * Return the TileData for the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return
	 */
	public TileData getTile(int x, int y) {
		return this.tiles[x + y * this.width];
	}

	/**
	 * Set a block in the world at the given position.
	 * 
	 * @param block
	 *            Block to place.
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 */
	public void setBlock(Block block, int x, int y) {
		TileData data = this.getTile(x, y);

		data.block = block;
		data.blockHp = TileData.maxHp;
		data.data = 0;
	}

	/**
	 * Set a wall in the world at the given position.
	 * 
	 * @param wall
	 *            Wall to place.
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 */
	public void setWall(Wall wall, int x, int y) {
		TileData data = this.getTile(x, y);

		data.wall = wall;
		data.wallHp = TileData.maxHp;
		data.data = 0;
	}

	/**
	 * Set the data tag of the tile in the world at the given position.
	 * 
	 * @param data
	 *            Tag value.
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 */
	public void setData(byte data, int x, int y) {
		TileData tileData = this.getTile(x, y);
		tileData.data = data;
	}

	/**
	 * Return the data tag of the tile in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @returnd Data tag value.
	 */
	public byte getData(int x, int y) {
		TileData tileData = this.getTile(x, y);
		return tileData.data;
	}

	/**
	 * Return the block in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Block in world.
	 */
	public Block getBlock(int x, int y) {
		TileData data = this.getTile(x, y);
		return data.block;
	}

	/**
	 * Return the wall in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Wall in world.
	 */
	public Wall getWall(int x, int y) {
		TileData data = this.getTile(x, y);
		return data.wall;
	}

	/**
	 * Return the ID of the block in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Block ID in world.
	 */
	public int getBlockId(int x, int y) {
		if (!this.isInside(x, y)) {
			return 0;
		}

		TileData data = this.getTile(x, y);

		if (data.block == null) {
			return 0;
		}

		return data.block.getId();
	}

	/**
	 * Return the ID of the wall in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Wall ID in world.
	 */
	public int getWallId(int x, int y) {
		if (!this.isInside(x, y)) {
			return 0;
		}

		TileData data = this.getTile(x, y);

		if (data.wall == null) {
			return 0;
		}

		return data.wall.getId();
	}

	/**
	 * Retrieve the TileData for the encoded XY index.
	 * 
	 * @param index
	 *            Encoded XY index.
	 * @return
	 */
	public TileData getTileData(int index) {
		return this.tiles[index];
	}

	/**
	 * Return the list of entities in the world.
	 * 
	 * @return
	 */
	public List<Entity> getEntities() {
		return entities;
	}

	/**
	 * Return the world version.
	 * 
	 * @return World version.
	 */
	public int getVersion() {
		return this.version;
	}

	/**
	 * Return the world's width.
	 * 
	 * @return World width.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Return the world's height.
	 * 
	 * @return World height.
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Return the world's name.
	 * 
	 * @return World name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the {@link org.egordorichev.lasttry.world.World.EvilType EvilType}
	 * of the world.
	 * 
	 * @return EvilType of the world.
	 */
	public EvilType getEvilType() {
		return this.evilType;
	}

	/**
	 * Check if the world is in expert mode.
	 * 
	 * @return Is expert world.
	 */
	public boolean isExpert() {
		return this.expert;
	}

	/**
	 * Set world expert mode status.
	 * 
	 * @param expert
	 */
	public void setExpert(boolean expert) {
		this.expert = expert;
	}

	/**
	 * Enumeration for the type of terrain to generate when the world
	 */
	public enum EvilType {
		CORRUPTION, CRIMSON
	}

	public void addBiomeChecker() {
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

		scheduledExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				int totalEvilBlocks = 0;
				int totalEvilDesertBlocks = 0;
				int totalDesertBlocks = 0;

				int windowWidth = LastTry.getWindowWidth();
				int windowHeight = LastTry.getWindowHeight();
				int tww = windowWidth / Block.TEX_SIZE;
				int twh = windowHeight / Block.TEX_SIZE;
				int tcx = (int) LastTry.camera.getX() / Block.TEX_SIZE;
				int tcy = (int) LastTry.camera.getY() / Block.TEX_SIZE;

				int minY = Math.max(0, tcy - 2);
				int maxY = Math.min(height - 1, tcy + twh + 2);
				int minX = Math.max(0, tcx - 2);
				int maxX = Math.min(width - 1, tcx + tww + 2);

				for (int y = minY; y < maxY; y++) {
					for (int x = minX; x < maxX; x++) {
						TileData tileData = getTile(x, y);

						if(tileData.block != null) {
							switch(tileData.block.getId()) {
								case ItemID.ebonstoneBlock:
								case ItemID.purpleIceBlock:
								case ItemID.corruptThornyBushes:
								case ItemID.vileMushroom:
								case ItemID.crimstoneBlock:
								case ItemID.redIceBlock:
								case ItemID.viciousMushroom:
									totalEvilBlocks++;
								break;
								case ItemID.sandBlock:
									totalDesertBlocks++;
								break;
								case ItemID.ebonsandBlock:
								case ItemID.crimsandBlock:
									totalEvilDesertBlocks++;
								default: break;
								// TODO: other biomes
							}
						}
					}
				}
				if(totalEvilBlocks > 200) {
					LastTry.world.currentBiome = (LastTry.world.getEvilType() == EvilType.CORRUPTION) ? Biome.corruption : Biome.crimson;
				} else if(totalEvilDesertBlocks > 1000) {
					LastTry.world.currentBiome = (LastTry.world.getEvilType() == EvilType.CORRUPTION) ? Biome.corruptDesert : Biome.crimsonDesert;
				} else if(totalDesertBlocks > 1000) {
					LastTry.world.currentBiome = Biome.desert;
				} else {
					LastTry.world.currentBiome = Biome.forest;
				}
			}
		}, 0, 3, TimeUnit.SECONDS);
	}
}
