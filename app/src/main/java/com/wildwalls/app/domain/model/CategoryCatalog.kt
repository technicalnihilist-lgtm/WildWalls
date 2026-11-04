package com.wildwalls.app.domain.model

/**
 * The 24 categories offered by WildWalls. Every entry was verified against the live
 * Wikimedia Commons API (search returns photographs, not empty result sets).
 */
object CategoryCatalog {

    const val FILE_FILTER = "filetype:bitmap"

    val all: List<AnimalCategory> = listOf(
        AnimalCategory("lions", "Lions", "Panthera leo", "deepcat:\"Panthera leo\" $FILE_FILTER",
            listOf("Panthera leo", "lion")),
        AnimalCategory("tigers", "Tigers", "Panthera tigris", "deepcat:\"Panthera tigris\" $FILE_FILTER",
            listOf("Panthera tigris", "tiger")),
        AnimalCategory("wolves", "Wolves", "Canis lupus", "deepcat:\"Canis lupus\" $FILE_FILTER",
            listOf("Canis lupus", "wolf", "wolves")),
        AnimalCategory("foxes", "Foxes", "Vulpes vulpes", "deepcat:\"Vulpes vulpes\" $FILE_FILTER",
            listOf("Vulpes", "fox")),
        AnimalCategory("leopards", "Leopards", "Panthera pardus", "deepcat:\"Panthera pardus\" $FILE_FILTER",
            listOf("Panthera pardus", "leopard")),
        AnimalCategory("elephants", "Elephants", "Elephantidae", "deepcat:\"Elephantidae\" $FILE_FILTER",
            listOf("Elephantidae", "Elephant", "Loxodonta", "Elephas")),
        AnimalCategory("wild-dogs", "Wild Dogs", "Lycaon pictus", "deepcat:\"Lycaon pictus\" $FILE_FILTER",
            listOf("Lycaon", "wild dog", "painted dog")),
        AnimalCategory("bears", "Bears", "Ursidae", "deepcat:\"Ursidae\" $FILE_FILTER",
            listOf("Ursidae", "bear", "Ursus")),
        AnimalCategory("deer", "Deer", "Cervidae", "deepcat:\"Cervidae\" $FILE_FILTER",
            listOf("Cervidae", "deer", "Cervus", "Odocoileus")),
        AnimalCategory("horses", "Horses", "Equus ferus caballus", "deepcat:\"Horses\" $FILE_FILTER",
            listOf("Horse", "Equus")),
        AnimalCategory("cats", "Cats", "Felis catus", "deepcat:\"Felis silvestris catus\" $FILE_FILTER",
            listOf("Felis silvestris catus", "Felis catus", "Domestic cat")),
        AnimalCategory("dogs", "Dogs", "Canis lupus familiaris", "deepcat:\"Dogs\" $FILE_FILTER",
            listOf("Canis lupus familiaris", "Dog")),
        AnimalCategory("eagles", "Eagles", "Accipitridae", "deepcat:\"Accipitridae\" $FILE_FILTER",
            listOf("Accipitridae", "eagle", "Aquila", "Haliaeetus")),
        AnimalCategory("owls", "Owls", "Strigidae", "deepcat:\"Strigidae\" $FILE_FILTER",
            listOf("Strigidae", "owl", "Bubo", "Athene", "Tyto")),
        AnimalCategory("parrots", "Parrots", "Psittaciformes", "deepcat:\"Psittaciformes\" $FILE_FILTER",
            listOf("Psittac", "parrot", "Ara", "Cacatua")),
        AnimalCategory("snakes", "Snakes", "Serpentes", "deepcat:\"Serpentes\" $FILE_FILTER",
            listOf("Serpentes", "snake", "Colubridae", "Pythonidae", "Viperidae")),
        AnimalCategory("crocodiles", "Crocodiles", "Crocodylidae", "deepcat:\"Crocodylidae\" $FILE_FILTER",
            listOf("Crocodyl", "crocodile", "Alligator", "Caiman")),
        AnimalCategory("sharks", "Sharks", "Selachimorpha", "deepcat:\"Selachimorpha\" $FILE_FILTER",
            listOf("Selachimorpha", "shark", "Carcharhinus", "Galeocerdo")),
        AnimalCategory("whales", "Whales", "Cetacea", "deepcat:\"Whales\" $FILE_FILTER",
            listOf("Whale", "Cetacea", "Megaptera", "Physeter", "Balaenoptera")),
        AnimalCategory("dolphins", "Dolphins", "Delphinidae", "deepcat:\"Delphinidae\" $FILE_FILTER",
            listOf("Delphinidae", "dolphin", "Tursiops", "Orcinus")),
        AnimalCategory("penguins", "Penguins", "Spheniscidae", "deepcat:\"Spheniscidae\" $FILE_FILTER",
            listOf("Spheniscidae", "penguin", "Aptenodytes", "Pygoscelis")),
        AnimalCategory("butterflies", "Butterflies", "Lepidoptera", "deepcat:\"Butterflies\" $FILE_FILTER",
            listOf("Butterfl", "Lepidoptera", "Papilio", "Nymphalidae")),
        AnimalCategory("reptiles", "Reptiles", "Reptilia", "deepcat:\"Reptilia\" $FILE_FILTER",
            listOf("Reptilia", "reptile", "Squamata", "Testudines", "Lacertilia")),
        // The Commons "Endangered species" tree already restricts results to listed species,
        // so no extra hint filter is applied here (files are rarely categorised by the word).
        AnimalCategory("endangered", "Endangered", "IUCN Red List", "deepcat:\"Endangered species\" $FILE_FILTER",
            emptyList())
    )

    /** Categories shown as quick chips on the home screen. */
    val popular: List<String> = listOf(
        "lions", "tigers", "elephants", "eagles", "wolves", "sharks", "penguins", "owls"
    )

    fun byId(id: String?): AnimalCategory? = all.firstOrNull { it.id == id }

    fun nameOf(id: String?): String = byId(id)?.name ?: "Wildlife"

    /** Vocabulary used for search suggestions (animal names only - never fake results). */
    val searchVocabulary: List<String> = listOf(
        "Lion", "Lioness", "Tiger", "Bengal tiger", "Siberian tiger", "White tiger", "Wolf", "Arctic wolf",
        "Grey wolf", "Fox", "Red fox", "Arctic fox", "Fennec fox", "Leopard", "Snow leopard", "Amur leopard",
        "Cheetah", "Jaguar", "Elephant", "African elephant", "Asian elephant", "Wild dog", "African wild dog",
        "Bear", "Polar bear", "Grizzly bear", "Panda", "Deer", "Elk", "Moose", "Horse", "Mustang", "Cat",
        "Kitten", "Dog", "Puppy", "Eagle", "Bald eagle", "Owl", "Snowy owl", "Parrot", "Macaw", "Snake",
        "Cobra", "Crocodile", "Alligator", "Shark", "Great white shark", "Whale", "Humpback whale",
        "Dolphin", "Penguin", "Butterfly", "Chameleon", "Tortoise", "Giraffe", "Zebra", "Rhinoceros",
        "Orangutan", "Gorilla", "Flamingo", "Peacock"
    )
}
