import java.text.NumberFormat
import javax.swing.JFrame

NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US)
// Use the following PrintStream for Unicode characters (card suits)
PrintStream out = new PrintStream(System.out, true, "UTF-8")

println "Welcome to Double Deck Blackjack at Aaron's Casino!"
println "House Rules: Split once per hand, dealer stands on all 17s."

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

    def show(String which, GamePanel gamePanel) {

        PrintStream out = new PrintStream(System.out, true, "UTF-8")

        def padding = 10

        println "-".padRight(padding+1, '-')
        println "| " + rank.padRight(padding-2) + "|"
        out.println "| " + suit.padRight(padding-2) + "|"
        2.times {
          println "|".padRight(padding) + "|"
        }
        out.println "|" + suit.padLeft(padding-2) + " |"
        println "|" + rank.padLeft(padding-2) + " |"
        println "-".padRight(padding+1, '-')

        gamePanel.updatePanel(which, this.imageFilename)
    }
}

class PlayerHand {
    ArrayList shoe
    int CUTCARD
    int playerBet
    BigDecimal playerStake
    int playerTotal
    int numPlayerAces
    int playerAcesAdjusted
    boolean playerHasBJ
    boolean splitAces

    PlayerHand (ArrayList tableShoe, int cutCard, int bet, BigDecimal stake, int total, int numAces, int acesAdjusted, boolean hasBJ, boolean splittingAces) {
        shoe = tableShoe
        CUTCARD = cutCard
        playerBet = bet
        playerStake = stake
        playerTotal = total
        numPlayerAces = numAces
        playerAcesAdjusted = acesAdjusted
        playerHasBJ = hasBJ
        splitAces = splittingAces
    }

    def playOut(GamePanel gamePanel) {

      if (!splitAces) {

        PLAYERHAND: while (shoe.size() > CUTCARD) {
            print "Stand(s)/Hit(h)/Double(d)? "
            def input = new BufferedReader(new InputStreamReader(System.in)).readLine()

            // If Double Down
            if (input.equalsIgnoreCase("D")) {
                if (playerBet*2 > playerStake) {
                    println "You don't have enough money to double your bet!"
                    continue PLAYERHAND
                } else {
                    playerBet *= 2
                }
            }

            // If Hit or Double
            if (!(input.equalsIgnoreCase("S"))) {

                def nextCard = shoe.remove(0)
                println "Player receives:"
                nextCard.show("player", gamePanel)
                Thread.sleep(1000)

                if (nextCard.rank == 'A') numPlayerAces++

                playerTotal += nextCard.value

                if (playerTotal > 21) {

                    // Check Aces and adjust as necessary
                    if (numPlayerAces > 0) {
                        for (int i=0; i < numPlayerAces-playerAcesAdjusted; i++) {
                          playerTotal -= 10
                          playerAcesAdjusted++
                          if (playerTotal < 22) break // for loop
                        }
                    }
                    if (playerTotal > 21) {
                        println "Player busted!"
                        playerStake -= playerBet
                        break PLAYERHAND
                    }
                }

                if (input.equalsIgnoreCase("D")) break PLAYERHAND


            } else { // Stand

                if (playerTotal > 21) {

                    // Check Aces and adjust as necessary
                    if (numPlayerAces > 0) {
                        for (int i=0; i < numPlayerAces-playerAcesAdjusted; i++) {
                          playerTotal -= 10
                          playerAcesAdjusted++
                          if (playerTotal < 22) break // for loop
                        }
                    }
                }

                break PLAYERHAND
            }
        } // end PLAYERHAND

       } else {

           // Split Aces, player hand receives no more cards
           if (playerTotal > 21) {
               // Player has AA
               playerTotal -= 10
               playerAcesAdjusted++
           }
       }

    }
}

Random generator = new Random()

final int ACE = 11
final int CUTCARD = generator.nextInt(5) + 5


// CLOSURES
def shuffle = { numDecks, addDeckClosure ->

    def shoe = new ArrayList()
    def combinedDecks = new ArrayList()

    numDecks.times {

        addDeckClosure(combinedDecks)
        
    }

    while (combinedDecks.size() > 0) {
        shoe.add(combinedDecks.remove(generator.nextInt(combinedDecks.size())))
    }

    //if (shoe.size() != numDecks*52)
    //    println "Shoe was not correctly generated! (Shoe size is ${shoe.size()})"

    return shoe
}

def dealerCanTakeCard = { numAces, numAdjusted, total ->
    // following check is for first two cards in dealer's hand being A,A
    if (numAces == 2 && numAdjusted == 0)
      return true
    // dealer stands on 17 or higher
    if (total < 17)
      return true

    return false
}

def showPair = { firstCard, secondCard ->

    def padding = 10

    println "-".padRight(padding+1, '-') + "  " + "-".padRight(padding+1, '-')
    print "| " + firstCard.rank.padRight(padding-2) + "|  "
    println "| " + secondCard.rank.padRight(padding-2) + "|"
    out.print "| " + firstCard.suit.padRight(padding-2) + "|  "
    out.println "| " + secondCard.suit.padRight(padding-2) + "|"
    2.times {
      println "|".padRight(padding) + "|  " + "|".padRight(padding) + "|"
    }
    out.print "|" + firstCard.suit.padLeft(padding-2) + " |  "
    out.println "|" + secondCard.suit.padLeft(padding-2) + " |"
    print "|" + firstCard.rank.padLeft(padding-2) + " |  "
    println "|" + secondCard.rank.padLeft(padding-2) + " |"
    println "-".padRight(padding+1, '-') + "  " + "-".padRight(padding+1, '-')
}

def processSplitHand = { splitHand, dealerTotal, dealerHasBJ, playerStake, which ->

    println "Player's " + which + " split hand totals " + splitHand.playerTotal
    if (dealerTotal < 22 && splitHand.playerTotal < 22) {
        if (dealerTotal > splitHand.playerTotal) {
            println "Dealer wins against " + which + " split hand"
            if (!dealerHasBJ)
                playerStake -= splitHand.playerBet
        }
        if (splitHand.playerTotal > dealerTotal) {
            println "Player wins " + which + " split hand!"
            if (splitHand.playerHasBJ)
                playerStake += splitHand.playerBet * 1.5
            else
                playerStake += splitHand.playerBet
        }
        if (splitHand.playerTotal == dealerTotal) {
            println "Push on " + which + " split hand..."
        }
    }

    return playerStake
}

def addDeck = { deck ->

    deck.add(new Card('A', Suit.CLUBS, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.CLUBS, i))
    }
    deck.add(new Card('J', Suit.CLUBS, 10))
    deck.add(new Card('Q', Suit.CLUBS, 10))
    deck.add(new Card('K', Suit.CLUBS, 10))

    deck.add(new Card('A', Suit.SPADES, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.SPADES, i))
    }
    deck.add(new Card('J', Suit.SPADES, 10))
    deck.add(new Card('Q', Suit.SPADES, 10))
    deck.add(new Card('K', Suit.SPADES, 10))


    deck.add(new Card('A', Suit.DIAMONDS, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.DIAMONDS, i))
    }
    deck.add(new Card('J', Suit.DIAMONDS, 10))
    deck.add(new Card('Q', Suit.DIAMONDS, 10))
    deck.add(new Card('K', Suit.DIAMONDS, 10))


    deck.add(new Card('A', Suit.HEARTS, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.HEARTS, i))
    }
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('Q', Suit.HEARTS, 10))
    deck.add(new Card('K', Suit.HEARTS, 10))

}

def testDeck = { deck ->
    deck.add(new Card('A', Suit.CLUBS, ACE))
    deck.add(new Card('A', Suit.HEARTS, ACE))
    deck.add(new Card('A', Suit.DIAMONDS, ACE))
    deck.add(new Card('A', Suit.SPADES, ACE))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('A', Suit.SPADES, ACE))
    deck.add(new Card('A', Suit.SPADES, ACE))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('A', Suit.SPADES, ACE))
    deck.add(new Card('A', Suit.SPADES, ACE))
    deck.add(new Card('A', Suit.SPADES, ACE))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('A', Suit.SPADES, ACE))
}
// END CLOSURES

def numOfDecks = 2
def shoe = shuffle(numOfDecks, addDeck)
def playerStake = 300

NEWHAND: while (shoe.size() > CUTCARD) {

    println "***************************************"
    Thread.sleep(2000)

    def gamePanel = new GamePanel()

    JFrame jframe = new JFrame()
    jframe.getContentPane().add(gamePanel)
    jframe.setSize(370,370)
    jframe.setTitle("Aaron's Casino Blackjack")
    jframe.setVisible(true)
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    if (playerStake <= 0) break NEWHAND

    print "Your stake is ${nf.format(playerStake)}. Enter bet or quit(q): "
    def betInput = new BufferedReader(new InputStreamReader(System.in)).readLine()

    try {
        playerBet = Integer.parseInt(betInput)
        
        if (playerBet == 0) break NEWHAND
        playerBet = playerBet.abs()
        if (playerBet > playerStake)
        {
            println "Bet of ${nf.format(playerBet)} exceeds Player Stake"
            continue NEWHAND
        }
    }
    catch (Exception e) {
        break NEWHAND
    }

    dealerTotal = 0
    numDealerAces = 0
    dealerAcesAdjusted = 0

    playerTotal = 0
    numPlayerAces = 0
    playerAcesAdjusted = 0

    card1 = shoe.remove(0)
    card2 = shoe.remove(0)
    card3 = shoe.remove(0)
    card4 = shoe.remove(0)

    println "Dealer is showing: "
    gamePanel.dealHoleCard()
    card4.show("dealer", gamePanel)

    println "You have: "
    showPair(card1, card3)
    gamePanel.updatePanel("player", card1.imageFilename)
    Thread.sleep(500)
    gamePanel.updatePanel("player", card3.imageFilename)

    if (card4.rank == 'A') {
        print "Do you want insurance (y/n)? "
        def insuranceInput = new BufferedReader(new InputStreamReader(System.in)).readLine()
        if (insuranceInput.equalsIgnoreCase("Y")) {
            if (playerStake >= playerBet/2) {
                if (card2.value == 10) {
                    playerStake += playerBet
                    println "Player won insurance bet!"
                } else {
                    playerStake -= playerBet/2
                    println "Player lost insurance bet - stake is now ${nf.format(playerStake)}"
                }
            } else
                println "Sorry, you don't have enough money to insure your bet"
        }
    }

    dealerTotal = card2.value + card4.value
    if (card2.rank == 'A') numDealerAces++
    if (card4.rank == 'A') numDealerAces++

    playerTotal = card1.value + card3.value
    if (card1.rank == 'A') numPlayerAces++
    if (card3.rank == 'A') numPlayerAces++

    dealerHasBJ = false
    if (dealerTotal == 21) {
        println "Dealer has blackjack!"
        dealerHasBJ = true
    }

    playerHasBJ = false
    if (playerTotal == 21) {
        println "Player has blackjack!"
        playerHasBJ = true
    }

    if (dealerHasBJ && playerHasBJ)
      println "What are the odds!"
    else if (playerHasBJ)
      playerStake += playerBet * 1.5
    else if (dealerHasBJ)
      playerStake -= playerBet

    def playerHand = new PlayerHand(shoe, CUTCARD, playerBet, playerStake, playerTotal, numPlayerAces, playerAcesAdjusted, false, false)
    def split1Hand = null
    def split2Hand = null
    def isSplit = false
    
    if (!dealerHasBJ && !playerHasBJ) {

        if (card1.rank.equals(card3.rank)) {
            print "Do you wish to split (y/n)? "
            def splitInput = new BufferedReader(new InputStreamReader(System.in)).readLine()
            if (splitInput.equalsIgnoreCase("Y")) isSplit = true
        }

        if (!isSplit) {

            playerHand.playOut(gamePanel)
            playerStake = playerHand.playerStake // account for player bust
            playerBet = playerHand.playerBet // account for double down bet and not busted
            playerTotal = playerHand.playerTotal
            
        } else {
            splittingAces = false
            if (card1.rank == 'A') splittingAces = true

            split1 = shoe.remove(0)
            split2 = shoe.remove(0)

            println "\nFirst split hand:"
            showPair(card1, split1)
            gamePanel.updatePanel("player", card1.imageFilename)
            gamePanel.updatePanel("player", split1.imageFilename)

            playerTotal = card1.value + split1.value
            numPlayerAces = 0
            playerAcesAdjusted = 0
            if (card1.rank == 'A') numPlayerAces++
            if (split1.rank == 'A') numPlayerAces++
            
            split1PlayerHasBJ = false
            if (playerTotal == 21 && !splittingAces) {
                println "Player has Blackjack on first split hand!"
                split1PlayerHasBJ = true
            }
            split1Hand = new PlayerHand(shoe, CUTCARD, playerBet, playerStake, playerTotal, numPlayerAces, playerAcesAdjusted, split1PlayerHasBJ, splittingAces)
            split1Hand.playOut(gamePanel)
            playerStake = split1Hand.playerStake // account for player bust on hit/double

            println "\nSecond split hand:"
            showPair(card3, split2)
            gamePanel.updatePanel("player", card3.imageFilename)
            gamePanel.updatePanel("player", split2.imageFilename)

            playerTotal = card3.value + split2.value
            numPlayerAces = 0
            playerAcesAdjusted = 0
            if (card3.rank == 'A') numPlayerAces++
            if (split2.rank == 'A') numPlayerAces++
            
            split2PlayerHasBJ = false
            if (playerTotal == 21 && !splittingAces) {
                println "Player has Blackjack on second split hand!"
                split2PlayerHasBJ = true
            }
            split2Hand = new PlayerHand(shoe, CUTCARD, playerBet, playerStake, playerTotal, numPlayerAces, playerAcesAdjusted, split2PlayerHasBJ, splittingAces)
            split2Hand.playOut(gamePanel)
            playerStake = split2Hand.playerStake // account for player bust on hit/double
        }

        DEALERHAND: while (shoe.size() > CUTCARD) {

            if ( (!isSplit && playerTotal < 22 && dealerCanTakeCard(numDealerAces, dealerAcesAdjusted, dealerTotal)) ||
                (isSplit && !(split1Hand.playerTotal > 21 && split2Hand.playerTotal > 21) && dealerCanTakeCard(numDealerAces, dealerAcesAdjusted, dealerTotal)) ) {

                def nextCard = shoe.remove(0)
                println "Dealer receives:"
                nextCard.show("dealer", gamePanel)

                if (nextCard.rank == 'A') numDealerAces++

                dealerTotal += nextCard.value

                if (dealerTotal > 21) {

                    // Check Aces and adjust as necessary
                    if (numDealerAces > 0) {
                        for (i=0; i < numDealerAces-dealerAcesAdjusted; i++) {
                          dealerTotal -= 10
                          dealerAcesAdjusted++
                          if (dealerTotal < 22) break // for loop
                        }
                    }
                    if (dealerTotal > 21) {
                        println "Dealer busted!"
                        if (!isSplit) {
                            playerStake += playerBet
                        } else {
                            // Only one or none of split hands busted.
                            // If hand did bust, playerStake already accounted.
                            if (split1Hand.playerTotal < 22)
                                playerStake += split1Hand.playerBet
                            if (split2Hand.playerTotal < 22)
                                playerStake += split2Hand.playerBet
                        }
                        break DEALERHAND
                    }
                }

          } else break DEALERHAND
        } // end DEALERHAND

    } // end No one has BJ

    Thread.sleep(2000)
    println "Dealer's other card was:"
    gamePanel.vanishHoleCard()
    card2.show("dealer", gamePanel)

    if (!isSplit) {
        println "Dealer totals " + dealerTotal + " and player totals " + playerTotal
        if (dealerTotal < 22 && playerTotal < 22) {
            if (dealerTotal > playerTotal) {
                println "Dealer wins"
                if (!dealerHasBJ)
                  playerStake -= playerBet
            }
            if (playerTotal > dealerTotal) {
                println "Player wins!"
                if (!playerHasBJ)
                  playerStake += playerBet
            }
            if (playerTotal == dealerTotal) {
                println "Push..."
            }
        }
    } else {
        
        println "Dealer totals " + dealerTotal

        playerStake = processSplitHand(split1Hand, dealerTotal, dealerHasBJ, playerStake, "first")

        playerStake = processSplitHand(split2Hand, dealerTotal, dealerHasBJ, playerStake, "second")

    }

} // end NEWHAND

println "Player walks away with ${nf.format(playerStake)}\n"