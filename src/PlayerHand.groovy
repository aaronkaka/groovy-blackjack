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

    def playOut(GamePanel gamePanel, boolean isFirstSplit = false, boolean isSecondSplit = false) {

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
                    nextCard.show("player", gamePanel, isFirstSplit, isSecondSplit) // pass through the split indicators
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