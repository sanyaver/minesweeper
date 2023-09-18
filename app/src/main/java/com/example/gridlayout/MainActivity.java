package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    boolean[][] bombGrid = new boolean[12][10]; // Initialize a 2D array to track bomb locations
    boolean flagbtn = false;
    boolean lost = false;
    boolean won = false;
    boolean[][] flagGrid = new boolean[12][10]; // Initialize a 2D array to track bomb locations
    boolean timerStart = false;

    int flagCount = 4;
    private int secondsPassed = 0; // Initial time in seconds

    ToggleButton toggleButton;
    TextView flagCounterTextView;


//    private Handler timeHandler = new Handler(Looper.getMainLooper());
    private Handler timeHandler = new Handler();




    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerStart = true;

        cell_tvs = new ArrayList<TextView>();
        toggleButton = findViewById(R.id.toggleButton);

        flagCounterTextView = findViewById(R.id.flagCounterTextView);

        // Update the flag count display
        updateFlagCountDisplay(flagCounterTextView);
        startTimer();




//        // Method (1): add statically created cells
//        TextView tv00 = (TextView) findViewById(R.id.textView00);
//        TextView tv01 = (TextView) findViewById(R.id.textView01);
//        TextView tv10 = (TextView) findViewById(R.id.textView10);
//        TextView tv11 = (TextView) findViewById(R.id.textView11);
//
//        tv00.setTextColor(Color.GRAY);
//        tv00.setBackgroundColor(Color.GRAY);
//        tv00.setOnClickListener(this::onClickTV);
//
//        tv01.setTextColor(Color.GRAY);
//        tv01.setBackgroundColor(Color.GRAY);
//        tv01.setOnClickListener(this::onClickTV);
//
//        tv10.setTextColor(Color.GRAY);
//        tv10.setBackgroundColor(Color.GRAY);
//        tv10.setOnClickListener(this::onClickTV);
//
//        tv11.setTextColor(Color.GRAY);
//        tv11.setBackgroundColor(Color.GRAY);
//        tv11.setOnClickListener(this::onClickTV);
//
//        cell_tvs.add(tv00);
//        cell_tvs.add(tv01);
//        cell_tvs.add(tv10);
//        cell_tvs.add(tv11);

        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);

        // Call the function to place bombs
        placeBombs(bombGrid);

        for (int i = 0; i<=11; i++) {
            for (int j=0; j<=9; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(28) );
                tv.setWidth( dpToPixel(28) );
                tv.setTextSize( 20 ); //dpToPixel(32)//
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }

        // Method (3): add four dynamically created cells with LayoutInflater
//        LayoutInflater li = LayoutInflater.from(this);
//        for (int i = 4; i<=5; i++) {
//            for (int j=0; j<=1; j++) {
//                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
//                //tv.setText(String.valueOf(i)+String.valueOf(j));
//                tv.setTextColor(Color.GRAY);
//                tv.setBackgroundColor(Color.GRAY);
//                tv.setOnClickListener(this::onClickTV);
//
//                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
//                lp.rowSpec = GridLayout.spec(i);
//                lp.columnSpec = GridLayout.spec(j);
//
//                grid.addView(tv, lp);
//
//                cell_tvs.add(tv);
//            }
//        }

    }


    private void updateFlagCountDisplay(TextView flagCounterTextView) {
        // Update the flag count display
        flagCounterTextView.setText("Flags: " + flagCount);
    }


    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private boolean flagMode = false;


    private void startTimer() {
        timeHandler.post(new Runnable() {
            TextView timeCounterTextView = findViewById(R.id.timeCounterTextView);

            @Override
            public void run() {
                if (timerStart) {
                    secondsPassed++;
                }
                String secPassed = Integer.toString(secondsPassed);
                timeCounterTextView.setText(secPassed + "s");
                timeHandler.postDelayed(this, 1000); // Delay 1 second (1000 milliseconds)
            }
        });

//        timeHandler.postDelayed((Runnable) this, 1000); // Initial delay 1 second (1000 milliseconds)
    }


    public void setFlagMode(boolean flagMode) {
        this.flagMode = flagMode;
        flagbtn = true;
    }

    // Function to count adjacent mines
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < bombGrid.length && newCol >= 0 && newCol < bombGrid[0].length && bombGrid[newRow][newCol]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void revealAdjacent(int row, int col) {
        TextView textView = cell_tvs.get(row * bombGrid[0].length + col);
        int adjacentMines = countAdjacentMines(row, col);
        if (adjacentMines == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int newRow = row + i;
                    int newCol = col + j;
                    if (newRow >= 0 && newRow < bombGrid.length && newCol >= 0 && newCol < bombGrid[0].length && !flagGrid[row][col]) {
                        textView.setText("0"); // Set "0" to indicate no adjacent mines
                        textView.setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
        }
        else {
            if (!flagGrid[row][col]) {
                textView.setText(String.valueOf(adjacentMines));
                // You can set different colors or backgrounds for different counts
                textView.setBackgroundColor(Color.WHITE);
            }
        }
    }

    // Function to check if all mines have corresponding flags or if the whole board except for four bombs has been unearthed
    private boolean allMinesFlaggedOrBoardUnearthed() {
        int revealedCount = 0; // Count of revealed cells

        for (int row = 0; row < bombGrid.length; row++) {
            for (int col = 0; col < bombGrid[0].length; col++) {
                if (bombGrid[row][col] && !flagGrid[row][col]) {
                    return false; // Found a mine without a flag
                }

                if (!bombGrid[row][col] && flagGrid[row][col]) {
                    return false; // Found a flag without a mine
                }

                if (!bombGrid[row][col] && !flagGrid[row][col] && !cell_tvs.get(row * bombGrid[0].length + col).getText().toString().isEmpty()) {
                    revealedCount++;
                }
            }
        }

        // Check if all but four cells have been revealed
        int totalCells = bombGrid.length * bombGrid[0].length;
        return revealedCount == (totalCells - 4);
    }



    private void revealCell(int row, int col) {
        if (row < 0 || row >= bombGrid.length || col < 0 || col >= bombGrid[0].length) {
            // Out of bounds
            return;
        }

        TextView textView = cell_tvs.get(row * bombGrid[0].length + col);

        if (textView.getText().toString() == "" && !flagGrid[row][col]) {

            // If the cell is not revealed

            int adjacentMines = countAdjacentMines(row, col);
            if (adjacentMines == 0) {
                // If no adjacent mines, recursively reveal adjacent cells
                textView.setText("0"); // Set "0" to indicate no adjacent mines
                textView.setBackgroundColor(Color.BLUE); // Update background color
                // Check adjacent cells recursively
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int newRow = row + i;
                        int newCol = col + j;
                        if (newRow >= 0 && newRow < bombGrid.length && newCol >= 0 && newCol < bombGrid[0].length) {
                            revealAdjacent(newRow, newCol);
                        }
                    }
                }
            } else {
                // If there are adjacent mines, display the count
                textView.setText(String.valueOf(adjacentMines));
                // You can set different colors or backgrounds for different counts
                textView.setBackgroundColor(Color.WHITE);
            }
        }
    }




    // You can also add a getter method to check the current flag mode status.
    public boolean isFlagMode() {
        return flagMode;
    }


    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;
        boolean won = allMinesFlaggedOrBoardUnearthed();

        // When the game ends
        if (won) {
            Intent intent = new Intent(this, DisplayActivity.class);
            String message = "You won! Good Job";
            intent.putExtra("com.example.GridLayout.MESSAGE", message);
            startActivity(intent);
//            finish(); // Optionally finish the current activity
        } else if (lost) {
            Intent intent = new Intent(this, DisplayActivity.class);
            String message = "You lost :(";
            intent.putExtra("com.example.GridLayout.MESSAGE", message);
            startActivity(intent);
//            finish(); // Optionally finish the current activity
        }

//        tv.setText(String.valueOf(i) + String.valueOf(j));
        if (!flagGrid[i][j]) {
            tv.setText("");
        }

        if (toggleButton.isChecked()) {
            // Flag mode is enabled
            toggleButton.setText(R.string.flag);
            setFlagMode(true);
        } else {
            // Dig mode is enabled
            toggleButton.setText(R.string.pick);
            setFlagMode(false);
        }


        if (bombGrid[i][j] && !flagMode && !flagGrid[i][j]) {
            lost = true;
            for (int row = 0; row < bombGrid.length; row++) {
                for (int col = 0; col < bombGrid[0].length; col++) {
                    if (bombGrid[row][col]) {
                        // Mark the bomb cells and show them

                        String bomb = getResources().getString(R.string.mine);

                        cell_tvs.get(row * COLUMN_COUNT + col).setText(bomb); // Set a text to indicate a bomb
                        cell_tvs.get(row * COLUMN_COUNT + col).setBackgroundColor(Color.RED);
                    }
                }
            }
        }
        else {
            if (flagMode) {

                if (flagGrid[i][j] == true) {
                    remove_flag(i, j);
                }
                else {
                    place_flag(i, j);
                }
            }
            else {
                revealCell(i, j);



            }
        }

    }

//    public class ResultActivity extends AppCompatActivity {
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.result_activity);
//
//            TextView resultMessageTextView = findViewById(R.id.resultMessageTextView);
//
//            // Retrieve the result message from the Intent
//            String resultMessage = getIntent().getStringExtra("resultMessage");
//
//            // Display the result message
//            resultMessageTextView.setText(resultMessage);
//        }
//    }


    void place_flag(int row, int col) {
        cell_tvs.get(row * COLUMN_COUNT + col).setText(getString(R.string.flag));
        flagGrid[row][col] = true;
        cell_tvs.get(row * COLUMN_COUNT + col).setBackgroundColor(Color.parseColor("lime"));
        flagCount--;
        updateFlagCountDisplay(flagCounterTextView);


    }
    void remove_flag(int row, int col) {
        cell_tvs.get(row * COLUMN_COUNT + col).setText("");
        flagGrid[row][col] = false;
        flagCount++;
        updateFlagCountDisplay(flagCounterTextView);

    }
    void placeBombs(boolean[][] bombGrid) {
        Random rand = new Random();
        int bombsPlaced = 0;

        while (bombsPlaced < 4) {
            int i = rand.nextInt(12); // Random row index (0-11)
            int j = rand.nextInt(10); // Random column index (0-9)

            if (!bombGrid[i][j]) {
                bombGrid[i][j] = true; // Place a bomb
                bombsPlaced++;
            }
        }
    }
}

