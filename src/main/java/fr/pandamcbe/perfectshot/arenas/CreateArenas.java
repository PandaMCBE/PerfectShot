package fr.pandamcbe.perfectshot.arenas;

import fr.pandamcbe.mGConstructorAPI.arenas.Arena;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class CreateArenas {

    private final List<Arena> arenas; //Instanciation de type privée et constante d'une ArrayList contenant les pojos Arena

    public List<Arena> getArenas() { //getter de cette liste
        return arenas;
    }

    public CreateArenas() {
        arenas = new ArrayList<>(); //Au début de la méthode, quand elle est chargée au démarrage, elle est d'abord initialisée à une nouvelle liste, c'est un constructeur qui doit évidemment être appelé qu'une fois.
        PftshotManager pftshotManager = PerfectShot.getInstance().getArenaManager();
        PftShotArena arena1 = new PftShotArena(new Location(Bukkit.getWorld("world"), -20, 16, 50), "a#1", pftshotManager.getPrefix(), 5, 2, 16); //On définit le centre de l'arène en même temps que le monde et puis le nom de l'arène
        arena1.getSpawns().add(new Location(arena1.getMiddlespawn().getWorld(), -20, 3, 55, -180.0f, 1.0f));//On ajoute tous les spawnpoints en se basant sur le monde retourné par le getter de la location du centre de l'arène
        arena1.getSpawns().add(new Location(arena1.getMiddlespawn().getWorld(), -24, 3, 51, -90.0f, 1.0f));
        arena1.getSpawns().add(new Location(arena1.getMiddlespawn().getWorld(), -20, 3, 45, 1.0f, 10.0f));
        arena1.getSpawns().add(new Location(arena1.getMiddlespawn().getWorld(), -16, 3, 50, 90.0f, -10.0f));
        arena1.getWaitingRoom().setLocation(new Location(Bukkit.getWorld("world"), -6.626, 52, -28.721, -0.0f, -0.0f));
        arenas.add(arena1); //On ajoute le nouvel objet arena à la liste

        PftShotArena arena2 = new PftShotArena(new Location(Bukkit.getWorld("world"), -30, 16, 50), "a#2", pftshotManager.getPrefix(), 5, 2, 8);
        arena2.getSpawns().add(new Location(arena2.getMiddlespawn().getWorld(), -30, 3, 55, -180.0f, 1.0f));
        arena2.getSpawns().add(new Location(arena2.getMiddlespawn().getWorld(), -34, 3, 51, -90.0f, 1.0f));
        arena2.getSpawns().add(new Location(arena2.getMiddlespawn().getWorld(), -30, 3, 45, 1.0f, 10.0f));
        arena2.getSpawns().add(new Location(arena2.getMiddlespawn().getWorld(), -26, 3, 50, 90.0f, -10.0f));
        arena2.getWaitingRoom().setLocation(new Location(Bukkit.getWorld("world"), -6.626, 52, -28.721, -0.0f, -0.0f));
        arenas.add(arena2);

    }

}