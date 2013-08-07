enum Suit {
    CLUBS("\u2663"),
    SPADES("\u2660"),
    DIAMONDS("\u2666"),
    HEARTS("\u2665")

    def representation

    Suit(symbol) {
        this.representation = symbol
    }

    @Override
    public String toString() {
        return "$representation"
    }
}

class Card {
    String rank
    String suit
    int value
    String imageFilename

    Card (String r, Suit s, int v) {
        rank = r
        suit = s
        value = v

        switch ( s ) {
            case Suit.CLUBS:
                imageFilename = "clubs"
                break;
            case Suit.DIAMONDS:
                imageFilename = "diamonds"
                break;
            case Suit.HEARTS:
                imageFilename = "hearts"
                break;
            case Suit.SPADES:
                imageFilename = "spades"
                break;
        }

        imageFilename += "-" + rank.toLowerCase() + "-75.png"
    }

    def show(String which, GamePanel gamePanel, boolean isFirstSplit = false, boolean isSecondSplit = false) {

        // Use the following PrintStream for Unicode characters (card suits)
        PrintStream out = new PrintStream(System.out, true, "UTF-8")

        def padding = 10

        out.println "-".padRight(padding+1, '-')
        out.println "| " + rank.padRight(padding-2) + "|"
        out.println "| " + suit.padRight(padding-2) + "|"
        2.times {
            out.println "|".padRight(padding) + "|"
        }
        out.println "|" + suit.padLeft(padding-2) + " |"
        out.println "|" + rank.padLeft(padding-2) + " |"
        out.println "-".padRight(padding+1, '-')

        gamePanel.updatePanel(which, this.imageFilename, isFirstSplit, isSecondSplit)
    }
}