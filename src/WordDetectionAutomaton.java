import java.util.*;

/**
 * @author Gonzalo De Achaval
 * @author Marcos Khabie
 * @author Agustin Bettati
 * @version 1.0
 */
public class WordDetectionAutomaton {
    private State initialState;
    private State currentState;
    private List<String> phrases;

    public WordDetectionAutomaton(List<String> phrases){
        initialState = new State("0");
        currentState = initialState;
        this.phrases = phrases;
        generateAutomaton(phrases);
    }

    private WordDetectionAutomaton(State initialState, List<String> phrases){
        this.initialState = initialState;
        currentState = initialState;
        this.phrases = phrases;
    }

    private void generateAutomaton(List<String> phrases) {
        int name = 1;
        for (String phrase : phrases) {
            //program is case insensitive
            String lowerCase = phrase.toLowerCase();

            char[] characters = lowerCase.toCharArray();

            State aux = initialState;
            for (int i = 0; i <= characters.length -2; i++) {
                State newState = new State(name + "");
                name++;
                aux.addNewTransition(characters[i], newState);
                aux = newState;
            }
            State finalState = new State(name+"", phrase);
            name++;
            aux.addNewTransition(characters[characters.length - 1], finalState);

        }
    }

    public WordDetectionAutomaton createDeterministic(){
        State determinState = new State("0");

        State nonDeterminState = initialState;
        makeDeterministic(determinState, Arrays.asList(nonDeterminState));

        return new WordDetectionAutomaton(determinState, phrases);
    }

    private void makeDeterministic(State determinState, List<State> nonDeterminStates) {
        Map<Character, List<State>> determinTransitions = new HashMap<>();
        Map<Character, String> nameOfNewStates = new HashMap<>();

        for (State nonDeterminState : nonDeterminStates) {
            //Si ya es el ultimo
            if(nonDeterminState.isEndingState()){
                String endWord = nonDeterminState.getEndingWord();
                determinState.convertToEndingState(endWord);
            }
            else{
                Map<Character, List<State>> trans = nonDeterminState.getTransitions();
                for (Map.Entry<Character, List<State>> transition : trans.entrySet()){
                    char character = transition.getKey();
                    for (State state : transition.getValue()) {
                        //agregar a determin refs los states para el char
                        addToMap(character,state, determinTransitions);
                        addToMap(character, state.getName(), nameOfNewStates);
                    }
                }
                //para cada char creo nuevo estado, le pongo la ref y llamo al metodo recursivo
            }

        }

        for (Map.Entry<Character, List<State>> transitions : determinTransitions.entrySet()) {
            State newState = new State(nameOfNewStates.get(transitions.getKey()));
            determinState.addNewTransition(transitions.getKey(), newState);
            makeDeterministic(newState, transitions.getValue());

        }

    }

    private void addToMap(char character, String name, Map<Character, String> map) {
        if(map.containsKey(character)){
            map.put(character,map.get(character) +" " + name);
        }
        else{
            map.put(character,name);
        }
    }

    private void addToMap(char character, State state, Map<Character, List<State>> map) {
        if(map.containsKey(character)){
            map.get(character).add(state);
        }
        else{
            List<State> states = new ArrayList<>();
            states.add(state);
            map.put(character, states);
        }
    }

    public Map<String, Integer> getFrequencies(String text){
        String lowerCase = text.toLowerCase();
        char[] array = lowerCase.toCharArray();
        Map<String, Integer> frequencies = new HashMap<>();
        for (String phrase : phrases) {
            frequencies.put(phrase, 0);
        }

        char prevChar = 'a';
        for (char character : array) {
            if(currentState.hasTransition(character)){
                List<State> listOfStates= currentState.getTransitionStates(character);
                //asumo que estoy llamando al metodo en un determinista
                currentState = listOfStates.get(0);
            }
            else {
                currentState = initialState;
                if(prevChar == ' '){
                    if(currentState.hasTransition(character)){
                        List<State> listOfStates= currentState.getTransitionStates(character);
                        //asumo que estoy llamando al metodo en un determinista
                        currentState = listOfStates.get(0);
                    }
                }
            }

            if(currentState.isEndingState()){
                String word = currentState.getEndingWord();
                frequencies.put(word,frequencies.get(word) + 1);
                // TODO discutir si aca se vuelve a q0 de una
            }

            prevChar = character;
        }

        return frequencies;
    }





}
