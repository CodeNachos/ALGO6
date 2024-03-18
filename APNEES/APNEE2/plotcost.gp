# gnuplot script (plotcost.gp)
set terminal png
set output 'cost.png'
set title 'Time of Execution'
set xlabel 'String Length'
set ylabel 'Time(s)'
plot  'dataDB.txt' using 1:2 with linespoints linewidth 3  title 'enumeration', 'dataDP.txt' using 1:2 with linespoints linewidth 3 title 'dynamic prog'
