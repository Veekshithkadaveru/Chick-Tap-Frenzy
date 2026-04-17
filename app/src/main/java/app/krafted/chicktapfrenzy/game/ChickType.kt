package app.krafted.chicktapfrenzy.game

enum class ChickType(
    val characterId: Int,
    val scoreValue: Int
) {
    COMMON_1(characterId = 1, scoreValue = 1),
    COMMON_2(characterId = 2, scoreValue = 1),
    COMMON_3(characterId = 3, scoreValue = 2),
    COMMON_4(characterId = 4, scoreValue = 2),
    SPRING(characterId = 5, scoreValue = 3),
    GOLDEN(characterId = 6, scoreValue = 5),
    FOX(characterId = 7, scoreValue = 0);

    val isHazard: Boolean
        get() = this == FOX

    val penalizesOnMiss: Boolean
        get() = this != FOX
}
