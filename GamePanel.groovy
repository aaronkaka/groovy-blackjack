import javax.swing.JPanel
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Color

class GamePanel extends JPanel {

    def images = new BufferedImage[3]
    def x_coord =  [35,145,255,35,150,255,35,150,255]
    def y_coord =  [50,50,50,140,150,150,245,245,245]
    def imagePath

    public GamePanel(String path) {

        imagePath = path
        this.setBackground(Color.green)

    }

    public void updatePanel() {

        images[0]  = ImageIO.read(new File(imagePath + "back-red-75-2.png"))
        images[1]  = ImageIO.read(new File(imagePath + "clubs-2-75.png"))

        repaint()
    }

    public void paint(Graphics g) {

        super.paintComponent(g)

        for(i in 0..1) {
            g.drawImage(images[i], x_coord[i], y_coord[i], null)
        }
    }

}