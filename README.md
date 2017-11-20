# MyAnimeListUserListsMerger
Creates a CSV that is a merged anime list of multiple MAL users from a textfile with the name "malusers.txt". In this file there should be the name of a MAL user or the link to their profile in each line. It will give out a "MergedLists.txt", that you can import to as example Excel or Calc with "^" as the cell seperator.

Also you need Commons IO: https://commons.apache.org/proper/commons-io/


TODO:

Change ignored entries of a specific user to only be the completed ones.

Add the option to not put out entries with a score count less than 3.

Add the option to put out scores with a decimal comma.
