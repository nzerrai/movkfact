package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur de noms de famille aléatoires français.
 * Sélectionne aléatoirement parmi 30 noms de famille courants.
 */
public class LastNameGenerator extends DataTypeGenerator {
    private static final List<String> FRENCH_LAST_NAMES = Arrays.asList(
        // Top 100 noms français (INSEE)
        "Martin", "Bernard", "Thomas", "Robert", "Richard",
        "Petit", "Durand", "Lefevre", "Michel", "Garcia",
        "David", "Bertrand", "Roux", "Vincent", "Fournier",
        "Morel", "Girard", "Andre", "Leroy", "Moreau",
        "Schmitt", "Mathieu", "Fontaine", "Chevalier", "Robin",
        "Fabre", "Nicolas", "Legrand", "Garnier", "Perrin",
        "Gauthier", "Clement", "Lambert", "Rousseau", "Renard",
        "Simon", "Blanchard", "Guerin", "Moulin", "Charpentier",
        "Colin", "Masson", "Ferreira", "Noel", "Caron",
        "Picard", "Carpentier", "Lacroix", "Barbier", "Dupont",
        "Leblanc", "Marchand", "Denis", "Lemaire", "Henry",
        "Hubert", "Grondin", "Olivier", "Brun", "Leclerc",
        "Poulain", "Bourgeois", "Tessier", "Perez", "Lopez",
        "Gonzalez", "Rodriguez", "Fernandez", "Martinez", "Sanchez",
        "Aubert", "Dumas", "Adam", "Leclercq", "Benoit",
        "Gautier", "Perrot", "Maillard", "Roussel", "Arnaud",
        "Vidal", "Dupuis", "Guillot", "Roger", "Vallet",
        "Barre", "Collet", "Lebrun", "Gillet", "Lefebvre",
        "Paget", "Menard", "Royer", "Pichon", "Bailly",
        "Guillon", "Levy", "Gros", "Descamps", "Fleury",
        "Poirier", "Lacombe", "Baudoin", "Breton", "Chauvin",
        // Noms occitans et du sud
        "Bonnet", "Cazaux", "Larrieu", "Pech", "Lagarde",
        "Soulier", "Lacaze", "Bousquet", "Abadie", "Laborde",
        "Monceau", "Pages", "Serres", "Causse", "Lacan",
        // Noms alsaciens / germaniques francisés
        "Muller", "Schneider", "Wagner", "Hoffmann", "Klein",
        "Bauer", "Werner", "Kremer", "Stein", "Huber",
        // Noms bretons et celtiques
        "Le Gall", "Le Bihan", "Le Roux", "Le Brun", "Le Meur",
        "Kermarrec", "Tréguier", "Bodilis", "Conan", "Gourvil",
        // Noms d'origine ibérique (Portugal & Espagne)
        "Pires", "Lopes", "Ferreira", "Silva", "Santos",
        "Costa", "Oliveira", "Sousa", "Alves", "Gomes",
        "Dias", "Rodrigues", "Pereira", "Nunes", "Cardoso",
        "Torres", "Jimenez", "Romero", "Alonso", "Moreno",
        "Ortega", "Castro", "Ruiz", "Navarro", "Dominguez",
        // Noms maghrébins courants en France
        "Benali", "Bensalem", "Benhamou", "Bouazza", "Chaabane",
        "Gharbi", "Hadj", "Hamdi", "Kacem", "Khelil",
        "Mansouri", "Mellouk", "Messaoudi", "Naji", "Ouali",
        "Rahmani", "Saidi", "Slimani", "Touati", "Ziani",
        "Amrani", "Belkacem", "Bouzid", "Chafai", "Daoudi",
        "Meziane", "Boudiaf", "Benabdallah", "Brahimi", "Cherif",
        "Achour", "Aouad", "Azoulay", "Badis", "Belmahi",
        "Berrada", "Boukhari", "Brahim", "Driss", "El Amrani",
        "Hamdouch", "Haouat", "Idrissi", "Lahlou", "Larbi",
        // Noms tunisiens
        "Ayari", "Ben Salah", "Chahed", "Dridi", "Ghanmi",
        "Hamrouni", "Jebali", "Karoui", "Mekni", "Nefzi",
        // Noms d'Afrique subsaharienne
        "Diallo", "Konaté", "Coulibaly", "Traoré", "Koné",
        "Touré", "Diop", "Ndiaye", "Fall", "Mbaye",
        "Keita", "Camara", "Bah", "Barry", "Sow",
        "Cissé", "Sylla", "Baldé", "Diouf", "Gueye",
        "Sané", "Faye", "Thiaw", "Sarr", "Tall",
        "Badji", "Bassène", "Diatta", "Tendeng", "Sambou",
        "Kaboré", "Zongo", "Sawadogo", "Ouédraogo", "Tiendrebeogo",
        "Toure", "Kouyaté", "Diabaté", "Doumbia", "Sanogo",
        // Noms des Antilles & DOM-TOM
        "Désir", "Élysée", "Ézelin", "Fanfan", "Fortuné",
        "Manioc", "Pied", "Rosette", "Saint-Louis", "Zenon",
        // Noms européens variés
        "Müller", "Schmidt", "Weber", "Becker", "Schulz",
        "Hoffmann", "Fischer", "Schäfer", "Koch", "Bauer",
        "Rossi", "Ferrari", "Russo", "Romano", "Ricci",
        "Esposito", "Bianchi", "Conti", "De Luca", "Mancini",
        "Kowalski", "Nowak", "Wiśniewski", "Wójcik", "Kowalczyk",
        "Kaminski", "Lewandowski", "Zielinski", "Szymanski", "Woźniak",
        "Dubois", "Lecomte", "Lepage", "Dupré", "Vilain",
        // Noms d'Asie du Sud-Est
        "Nguyen", "Tran", "Le", "Pham", "Hoang",
        "Vo", "Bui", "Dang", "Ngo", "Do",
        "Chen", "Wang", "Li", "Zhang", "Liu"
    );

    private static final Random random = new Random();

    public LastNameGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }
    /**
     * Génère un nom de famille aléatoire.
     * @return String - Nom de famille généré (ex: "Dupont", "Martin", "Bernard")
     */    @Override
    public Object generate() {
        return FRENCH_LAST_NAMES.get(random.nextInt(FRENCH_LAST_NAMES.size()));
    }
}
