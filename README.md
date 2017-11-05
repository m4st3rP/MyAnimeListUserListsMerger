# MyAnimeListUserListsMerger
Creates a CSV that is a merged anime list of multiple MAL users from a textfile with the name "malusers.txt". In this file there should be the name of a MAL user in each line. It will give out a "MergedLists.txt", that you can import it to Excel, "^" is the cell seperator.
I haven't included my formula for a weighted score in the program itself yet, it is: (Score x 0.9) + ((ScoreCount/MaxScoreCount) x 0.1) x 12
