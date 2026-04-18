package app.krafted.chicktapfrenzy.game

enum class ChickType(
    val characterId: Int,
    val scoreValue: Int
) {
    CHICKEN(characterId = 5, scoreValue = 1),
    GOLDEN(characterId = 6, scoreValue = 5),
    FOX(characterId = 7, scoreValue = 0);

    val isHazard: Boolean
        get() = this == FOX

    val penalizesOnMiss: Boolean
        get() = this != FOX
}
