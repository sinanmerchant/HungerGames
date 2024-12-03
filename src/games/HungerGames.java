package games;

import java.util.ArrayList;

/**
 * This class contains methods to represent the Hunger Games using BSTs.
 * Moves people from input files to districts, eliminates people from the game,
 * and determines a possible winner.
 * 
 * @author Pranay Roni
 * @author Maksims Kurjanovics Kravcenko
 * @author Kal Pandit
 * @author Sinan Merchant
 */
public class HungerGames {

    private ArrayList<District> districts;  // all districts in Panem.
    private TreeNode            game;       // root of the BST. The BST contains districts that are still in the game.

    /**
     * Default constructor, initializes a list of districts.
     */
    public HungerGames() {
        districts = new ArrayList<>();
        game = null;
        StdRandom.setSeed(2023);
    }

    /**
     * Sets up Panem, the universe in which the Hunger Games takes place.
     * Reads districts and people from the input file.
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPanem(String filename) { 
        StdIn.setFile(filename);  // open the file - happens only once here
        setupDistricts(filename); 
        setupPeople(filename);
    }

    /**
     * Reads the following from input file:
     * - Number of districts
     * - District ID's (insert in order of insertion)
     * Insert districts into the districts ArrayList in order of appearance.
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupDistricts (String filename) {

        // Read the number of districts from the input file
        int numDistricts = StdIn.readInt();

        // Create an empty ArrayList to store the districts
        ArrayList<District> districts = new ArrayList<>();

        // Read the district IDs from the input file
        for (int i = 0; i < numDistricts; i++) {
            int districtId = StdIn.readInt();

            // Create a new District object with the given ID
            District district = new District(districtId);

            // Add the district to the ArrayList in order of appearance
            districts.add(district);
        }

        // Set the districts field to the ArrayList
        this.districts = districts;
    }

    /**
     * Reads the following from input file (continues to read from the SAME input file as setupDistricts()):
     * Number of people
     * Space-separated: first name, last name, birth month (1-12), age, district id, effectiveness
     * Districts will be initialized to the instance variable districts
     * 
     * Persons will be added to corresponding district in districts defined by districtID
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPeople (String filename) {

        int numPeople = StdIn.readInt();

        // Read the people from the input file
        for (int i = 0; i < numPeople; i++) {
            String firstName = StdIn.readString();
            String lastName = StdIn.readString();
            int birthMonth = StdIn.readInt();
            int age = StdIn.readInt();
            int districtID = StdIn.readInt();
            int effectiveness = StdIn.readInt();

            // Create a new Person object with the given information
            Person person = new Person(birthMonth, firstName, lastName, age, districtID, effectiveness);

            // Determine whether the person has the Tessera property
            boolean tessera = age >= 12 && age < 18;
            person.setTessera(tessera);
            
            for(int j = 0; j < districts.size(); j++){
                if(districts.get(j).getDistrictID() == districtID) {
                    if(person.getBirthMonth() % 2 == 0) {
                        districts.get(j).addEvenPerson(person);
                    } else {
                        districts.get(j).addOddPerson(person);
                    }
                }
            }
        }
    }

    /**
     * Adds a district to the game BST.
     * If the district is already added, do nothing
     * 
     * @param root        the TreeNode root which we access all the added districts
     * @param newDistrict the district we wish to add
     */
    public void addDistrictToGame(TreeNode root, District newDistrict) {

        if(game == null){
            game = new TreeNode(newDistrict, null, null);
            districts.remove(newDistrict);
            return;
        }

        int id = newDistrict.getDistrictID();
        TreeNode current = game;

        while(current != null){
            int currentId = current.getDistrict().getDistrictID();
            if(id > currentId){
                if(current.getRight() == null){
                    current.setRight(new TreeNode(newDistrict, null, null));
                    break;
                } 
                current = current.getRight();
            }else{
                if(current.getLeft() == null){
                    current.setLeft(new TreeNode(newDistrict, null, null));
                    break;
                } 
                current = current.getLeft();
            }
        }

        districts.remove(newDistrict);
    }

    /**
     * Searches for a district inside of the BST given the district id.
     * 
     * @param id the district to search
     * @return the district if found, null if not found
     */
    public District findDistrict(int id) {
        return findDistrictRecursive(game, id);
    }

    private District findDistrictRecursive(TreeNode root, int id){
        if(root == null){
            return null;
        }

        int rootId = root.getDistrict().getDistrictID();

        if(rootId == id){
            return root.getDistrict();
        }

        if(id > rootId){
            return findDistrictRecursive(root.getRight(), id);
        }

        return findDistrictRecursive(root.getLeft(), id);
    }

    /**
     * Selects two duelers from the tree, following these rules:
     * - One odd person and one even person should be in the pair.
     * - Dueler with Tessera (age 12-18, use tessera instance variable) must be
     * retrieved first.
     * - Find the first odd person and even person (separately) with Tessera if they
     * exist.
     * - If you can't find a person, use StdRandom.uniform(x) where x is the respective 
     * population size to obtain a dueler.
     * - Add odd person dueler to person1 of new DuelerPair and even person dueler to
     * person2.
     * - People from the same district cannot fight against each other.
     * 
     * @return the pair of dueler retrieved from this method.
     */
    public DuelPair selectDuelers() {
        
        Person oddPerson = findDuelerRecursive(game, false, true, -1);
        Person evenPerson = findDuelerRecursive(game, true, true, oddPerson != null ? oddPerson.getDistrictID() : -1);

        if(oddPerson == null){
            oddPerson = findDuelerRecursive(game, false, false, evenPerson != null ? evenPerson.getDistrictID() : -1);
        }

        if(evenPerson == null){
            evenPerson = findDuelerRecursive(game, true, false, oddPerson != null ? oddPerson.getDistrictID() : -1);
        }

        if(oddPerson != null){
            District oddPersonDistrict = findDistrict(oddPerson.getDistrictID());
            oddPersonDistrict.getOddPopulation().remove(oddPerson);
        }
        
        if(evenPerson != null){
            District evenPersonDistrict = findDistrict(evenPerson.getDistrictID());
            evenPersonDistrict.getEvenPopulation().remove(evenPerson);
        }

        return new DuelPair(oddPerson, evenPerson); // update this line
    }

    private Person findDuelerRecursive(TreeNode root, boolean findEven, boolean useTessera, int districtToExclude){
        if(root == null){
            return null;
        }
        Person dueler = findDueler(root.getDistrict(), findEven, useTessera, districtToExclude);
        if(dueler == null){
            dueler = findDuelerRecursive(root.getLeft(), findEven, useTessera, districtToExclude);
        }
        if(dueler == null){
            dueler = findDuelerRecursive(root.getRight(), findEven, useTessera, districtToExclude);
        }
        return dueler;
    }

    private Person findDueler(District district, boolean findEven, boolean useTessera, int districtToExclude){
        if(district.getDistrictID() == districtToExclude){
            return null;
        }
        Person dueler = null;
        ArrayList<Person> population = district.getOddPopulation();
        if(findEven){
            population = district.getEvenPopulation();
        }

        if(useTessera){
            int i = 0;
            while(i < population.size() && dueler == null){
                if(population.get(i).getTessera()){
                    dueler = population.get(i);
                }
                i++;
            }
        }else{
            dueler = population.get(StdRandom.uniform(population.size()));
        }
        
        return dueler;
    }


    /**
     * Deletes a district from the BST when they are eliminated from the game.
     * Districts are identified by id's.
     * If district does not exist, do nothing.
     * 
     * This is similar to the BST delete we have seen in class.
     * 
     * @param id the ID of the district to eliminate
     */
    public void eliminateDistrict(int id) {

        TreeNode parent = null;

        if(game.getDistrict().getDistrictID() != id){
            parent = findParentDistrict(game, id);
        }

        if(parent == null){
            if(game.getDistrict().getDistrictID() != id){
                return;
            }
            if(game.getLeft() != null && game.getRight() != null){
                TreeNode current = game.getRight();
                TreeNode previous = game;
                while(current.getLeft() != null){
                    previous = current;
                    current = current.getLeft();
                }
                game.setDistrict(current.getDistrict());
                if(previous == game){
                    previous.setRight(null);
                }else{
                    previous.setLeft(null);
                }
            }else if(game.getLeft() != null){
                game = game.getLeft();
            }else{
                game = game.getRight();
            }
            return;
        }

        if(parent.getLeft() != null && parent.getLeft().getDistrict().getDistrictID() == id){
            TreeNode districtToDelete = parent.getLeft();
            if(districtToDelete.getLeft() != null && districtToDelete.getRight() != null){
                TreeNode current = districtToDelete.getRight();
                TreeNode previous = districtToDelete;
                while(current.getLeft() != null){
                    previous = current;
                    current = current.getLeft();
                }
                districtToDelete.setDistrict(current.getDistrict());
                if(previous == districtToDelete){
                    previous.setRight(null);
                }else{
                    previous.setLeft(null);
                }
            }else if(districtToDelete.getLeft() != null){
                parent.setLeft(districtToDelete.getLeft());
            }else{
                parent.setLeft(districtToDelete.getRight());
            }
        }else{
            TreeNode districtToDelete = parent.getRight();
            if(districtToDelete.getLeft() != null && districtToDelete.getRight() != null){
                TreeNode current = districtToDelete.getRight();
                TreeNode previous = districtToDelete;
                while(current.getLeft() != null){
                    previous = current;
                    current = current.getLeft();
                }
                districtToDelete.setDistrict(current.getDistrict());
                if(previous == districtToDelete){
                    previous.setRight(null);
                }else{
                    previous.setLeft(null);
                }
            }else if(districtToDelete.getLeft() != null){
                parent.setRight(districtToDelete.getLeft());
            }else{
                parent.setRight(districtToDelete.getRight());
            }
        }
    }

    private TreeNode findParentDistrict(TreeNode root, int id){
        if(root == null){
            return null;
        }
        if(root.getLeft() != null && root.getLeft().getDistrict().getDistrictID() == id){
            return root;
        }
        if(root.getRight() != null && root.getRight().getDistrict().getDistrictID() == id){
            return root;
        }
        TreeNode parent = findParentDistrict(root.getLeft(), id);
        if(parent == null){
            parent = findParentDistrict(root.getRight(), id);
        }
        return parent;
    }

    /**
     * Eliminates a dueler from a pair of duelers.
     * - Both duelers in the DuelPair argument given will duel
     * - Winner gets returned to their District
     * - Eliminate a District if it only contains a odd person population or even
     * person population
     * 
     * @param pair of persons to fight each other.
     */
    public void eliminateDueler(DuelPair pair) {

        if(pair.getPerson1() == null || pair.getPerson2() == null){
            if(pair.getPerson1() != null){
                sendBack(pair.getPerson1());
            }else{
                sendBack(pair.getPerson2());
            }
            return;
        }

        Person winner = pair.getPerson1().duel(pair.getPerson2());
        Person loser = pair.getPerson1() == winner ? pair.getPerson2() : pair.getPerson1();

        sendBack(winner);
        checkDistrict(loser.getDistrictID());
    }

    private void sendBack(Person person){
        District district = findDistrict(person.getDistrictID());
        if(district == null){
            return;
        }
        if(person.getBirthMonth() % 2 == 0) {
            district.addEvenPerson(person);
        } else {
            district.addOddPerson(person);
        }
    }

    private void checkDistrict(int districtId){
        District district = findDistrict(districtId);
        if(district == null){
            return;
        }
        if(district.getOddPopulation().size() == 0 || district.getEvenPopulation().size() == 0){
            eliminateDistrict(districtId);
        }
    }

    /**
     * Obtains the list of districts for the Driver.
     * 
     * @return the ArrayList of districts for selection
     */
    public ArrayList<District> getDistricts() {
        return this.districts;
    }

    /**
     * Returns the root of the BST
     */
    public TreeNode getRoot() {
        return game;
    }
}