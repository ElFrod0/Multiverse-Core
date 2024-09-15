package org.mvplugins.multiverse.core.world

import org.bukkit.World
import org.bukkit.WorldType
import org.mvplugins.multiverse.core.TestWithMockBukkit
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import kotlin.test.*

class WorldManagerTest : TestWithMockBukkit() {

    private lateinit var worldManager: WorldManager
    private lateinit var world: LoadedMultiverseWorld

    @BeforeTest
    fun setUp() {
        worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service") }

        worldManager.createWorld(CreateWorldOptions.worldName("world"))
        world = worldManager.getLoadedWorld("world").get()
        assertNotNull(world)
    }

    @Test
    fun `Creates a new world`() {
        worldManager.createWorld(CreateWorldOptions.worldName("world_nether")
            .environment(World.Environment.NETHER)
            .generateStructures(false)
            .seed(1234L)
            .useSpawnAdjust(true)
            .worldType(WorldType.FLAT)
        )

        val world = worldManager.getLoadedWorld("world_nether").get()
        assertNotNull(world)
        assertEquals("world_nether", world.name)
        assertEquals(World.Environment.NETHER, world.environment)
        assertEquals("", world.generator)
        assertEquals(1234L, world.seed)
    }

    @Test
    fun `Delete world`() {
        worldManager.deleteWorld(world)
        assertFalse(worldManager.getLoadedWorld("world").isDefined)
    }
}
