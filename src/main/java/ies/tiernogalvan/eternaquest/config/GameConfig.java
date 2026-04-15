package ies.tiernogalvan.eternaquest.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration @Getter
public class GameConfig {

    @Value("${game.invasion.probability:0.30}")
    private double invasionProbability;

    @Value("${game.invasion.min-level-difference:0}")
    private int minLevelDifference;

    public record StatsBase(int vida, int ataque, int defensa, int velocidad, int magia, int mana) {}

    public StatsBase getStatsBase(ies.tiernogalvan.eternaquest.model.enums.ClasePersonaje clase) {
        return switch (clase) {
            case GUERRERO -> new StatsBase(160, 22, 15, 10, 3, 30);
            case MAGO     -> new StatsBase(90,  12,  6, 11, 20, 80);
            case ARQUERO  -> new StatsBase(110, 18,  8, 16, 5, 40);
        };
    }
}
