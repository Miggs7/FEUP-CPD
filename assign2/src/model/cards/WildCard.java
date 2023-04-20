package model.cards;

import game_interface.Card;
import game_interface.Constants.Color;
import game_interface.Constants.Value;

public class WildCard extends Card{
    private Color chosenColor;
    
    public WildCard(Value value) {
        super(Color.BLACK, value);
    }

    public void chooseColor(Color color) {
        this.chosenColor = color;
    }

    public Color getChosenColor() {
        return chosenColor;
    }
    
}
