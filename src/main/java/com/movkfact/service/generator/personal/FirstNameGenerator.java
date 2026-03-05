package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur de prénoms français.
 */
public class FirstNameGenerator extends DataTypeGenerator {
    private static final List<String> FRENCH_FIRST_NAMES = Arrays.asList(
        // Prénoms masculins français — générations 40-70 ans
        "Jean", "Pierre", "Marc", "Paul", "Luc", "Alain", "Denis", "Patrick",
        "Philippe", "Laurent", "Olivier", "Thierry", "Christophe", "François",
        "Michel", "Claude", "Bernard", "Daniel", "Jacques", "André",
        "René", "Gérard", "Raymond", "Roger", "Henri", "Robert", "Marcel",
        "Yves", "Serge", "Guy", "Rémi", "Didier", "Éric", "Pascal",
        "Frédéric", "Cédric", "Bertrand", "Gilles", "Bruno", "Xavier",
        // Prénoms masculins français — générations 20-40 ans
        "Nicolas", "Julien", "Thomas", "Alexandre", "Mathieu", "Antoine",
        "Guillaume", "Romain", "Clément", "Maxime", "Quentin", "Sébastien",
        "Damien", "Benoît", "Jérôme", "Arnaud", "Grégoire", "Stéphane",
        "Vincent", "Jonathan", "Ludovic", "Cyril", "Emmanuel", "Thibault",
        "Michaël", "Adrien", "Valentin", "Mathis", "Alexis", "Florian",
        // Prénoms masculins français — génération actuelle (moins de 20 ans)
        "Raphaël", "Hugo", "Léo", "Théo", "Lucas", "Noah", "Ethan",
        "Louis", "Nathan", "Adam", "Arthur", "Baptiste", "Dylan", "Enzo",
        "Gabriel", "Kevin", "Loïc", "Tristan", "Victor", "Yoann",
        "Mael", "Nolan", "Axel", "Tom", "Timothée", "Sacha", "Ilyes",
        "Yanis", "Rayan", "Ayoub", "Bilal", "Sami", "Wassim", "Nassim",
        // Prénoms masculins maghrébins & arabes
        "Mehdi", "Karim", "Youssef", "Ibrahim", "Omar", "Sofiane", "Amine",
        "Zakariya", "Hamza", "Hassan", "Moussa", "Rachid", "Tarek", "Walid",
        "Fouad", "Khalid", "Nabil", "Samir", "Aziz", "Hicham", "Jamal",
        // Prénoms masculins africains subsahariens
        "Mamadou", "Oumar", "Seydou", "Cheikh", "Abdou", "Boubacar",
        "Modou", "Lamine", "Tidiane", "Moussa", "Ibrahima", "Samba",
        // Prénoms masculins européens variés
        "Carlos", "Miguel", "Alejandro", "Sergio", "Luis", "Pedro",
        "Marco", "Andrea", "Luca", "Matteo", "Federico", "Giuseppe",
        "Lukas", "Tobias", "Fabian", "Jonas", "Moritz", "Sebastian",
        // Prénoms féminins français — générations 40-70 ans
        "Marie", "Sophie", "Anne", "Claire", "Isabelle", "Monique",
        "Jacqueline", "Nathalie", "Sylvie", "Martine", "Christine",
        "Valérie", "Catherine", "Bernadette", "Pascale", "Sandrine",
        "Véronique", "Michèle", "Claudine", "Andrée", "Josette",
        "Françoise", "Brigitte", "Odette", "Ginette", "Simone", "Denise",
        "Renée", "Yvette", "Gisèle", "Liliane", "Colette", "Paulette",
        // Prénoms féminins français — générations 20-40 ans
        "Céline", "Aurélie", "Émilie", "Julie", "Camille", "Laura",
        "Lucie", "Manon", "Océane", "Pauline", "Sarah", "Charlotte",
        "Alexandra", "Audrey", "Virginie", "Mélanie", "Elodie", "Laetitia",
        "Stéphanie", "Jennifer", "Jessica", "Sabrina", "Amandine", "Laure",
        // Prénoms féminins français — génération actuelle
        "Léa", "Inès", "Jade", "Emma", "Zoé", "Clara", "Alice",
        "Ambre", "Anaïs", "Elisa", "Eva", "Lena", "Lisa", "Lou",
        "Luna", "Maëlys", "Margot", "Mathilde", "Mélodie", "Nina",
        "Noémie", "Romane", "Lucie", "Chloé", "Justine", "Estelle",
        "Axelle", "Juliette", "Pauline", "Marion", "Elsa", "Lola",
        "Roxane", "Cassandre", "Gaëlle", "Typhaine", "Gwenaëlle",
        // Prénoms féminins maghrébins & arabes
        "Yasmine", "Fatima", "Khadija", "Nour", "Samira", "Leïla",
        "Amina", "Houda", "Soraya", "Rania", "Selma", "Dina", "Lina",
        "Hana", "Sara", "Malika", "Zineb", "Hajar", "Meryem", "Wafa",
        "Safia", "Naima", "Fatiha", "Hanane", "Soumia", "Loubna",
        // Prénoms féminins africains subsahariens
        "Aissatou", "Fatoumata", "Rokhaya", "Mariama", "Adja", "Coumba",
        "Ndéye", "Aminata", "Kadiatou", "Binta", "Mariam", "Hawa",
        // Prénoms féminins européens variés
        "Carmen", "Isabel", "Lucia", "Elena", "Rosa", "Pilar",
        "Giulia", "Valentina", "Francesca", "Chiara", "Silvia", "Alessia",
        "Hanna", "Katrin", "Annika", "Lena", "Mia", "Anna",
        // Prénoms mixtes & internationaux courants en France
        "Alex", "Sam", "Charlie", "Robin", "Sasha", "Kim", "Jordan",
        "Morgan", "Liam", "Aiden", "Oliver", "James", "Ella", "Ava",
        "Isabella", "Sophia", "Amelia", "Ryan", "Tyler", "Logan"
    );

    private static final Random random = new Random();

    public FirstNameGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }
    /**
     * Génère un prénom aléatoire.
     * @return String - Prénom généré (ex: "Marie", "Jean", "Sophie")
     */    @Override
    public Object generate() {
        return FRENCH_FIRST_NAMES.get(random.nextInt(FRENCH_FIRST_NAMES.size()));
    }
}
