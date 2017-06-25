/**
 * CS 112
 * Created by David Flores on 12/18/2015.
 * Program Title: DrawFromText
 * Program Description: a frame that takes in a file and draws it on screen
 *
 * Algorithm:
 *  Variables Needed:
 *      an array to store the data from the file
 *      an int to store the size of the picture
 *      a file to open
 *      a boolean whether color skewing is enabled
 *      ints for the maximum original shade and minimum original shade
 *
 *  Constructor:
 *      takes in the file to read from and th boolean for color skewing
 *      opens a scanner on the file
 *      reads in the first int and assigns it to the size
 *      reads in each line after that and assigns it to another scanner
 *      second scanner populates the array with the integers from the file
 *      calls repaint
 *
 *  skewColors
 *      takes an int to skew
 *      if skewColors is false, just return the value
 *      otherwise
 *          subtract minimum original shade from it
 *          multiply it by 255
 *          divide by the difference of the max and min original shade
 *
 *  paint
 *      use double for loop, using counters as coordinates
 *          access the value at the counters, and skew it
 *          use it to create a color
 *          set the color of the graphics content
 *          draw a line at the coordinates
 */

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DrawFromText extends JFrame
{
    private int[][] data;

	public static void main(String[] args) throws FileNotFoundException
    {
        File file = new File("..\\..\\..\\heatmap.txt");
        DrawFromText partA = new DrawFromText(file);
        partA.setTitle("Heat Map");
		partA.setResizable(true);
        partA.setVisible(true);
    }
	
    public DrawFromText(File file)
    {
        //initialize variables
        Scanner reader = null;
        try
        {
            reader = new Scanner(file);
        }
        catch (FileNotFoundException e)
        {
            int option = JOptionPane.showConfirmDialog(null, "File not found, would you like to choose the file?", "File not Found", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.OK_OPTION)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.showOpenDialog(null);
                file = chooser.getSelectedFile();
                try
                {
                    reader = new Scanner(file);
                }
                catch (FileNotFoundException e1)
                {
                    JOptionPane.showMessageDialog(null, "File not Found, closing window");
                    dispose();
                }
            }
            else
            {
                dispose();
            }
        }
        String line = "";
        Scanner lineReader = new Scanner(line);
        int sizeX = reader.nextInt();
		int sizeY = reader.nextInt();
        data = new int[sizeX][sizeY];

        //set up frame
        setSize(sizeX, sizeY);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //no data to read
        if (sizeX == 0)
        {
            return;
        }

        //clear the scanner
        reader.nextLine();
        for (int i = 0; i < sizeX; i++)
        {
            line = reader.nextLine();
            lineReader = new Scanner(line);
            for (int j = 0; j < sizeY; j++)
            {
                data[i][j] = lineReader.nextInt();
            }
        }

        repaint();
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Color color;

        for (int i = 0; i < data.length; i++)
        {
            for (int j = 0; j < data[i].length; j++)
            {
                int colorValue = data[i][j];
                color = new Color(colorValue, colorValue, colorValue);
                g.setColor(color);
                g.drawLine(i, j, i, j);
            }
        }
    }
}
