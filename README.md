# MyAnimeListUserListsMerger
Creates a CSV that is a merged anime list of multiple MAL users from a textfile with the name "malusers.txt". In this file there should be the name of a MAL user in each line. It will give out a "MergedLists.txt", that you can import to as example Excel or Calc with "^" as the cell seperator.

Also you need Commons IO: https://commons.apache.org/proper/commons-io/

TODO:
Add the option to ignore the completed entries of a specific user.

Add the option to not put out entries with a score count less than 3.

Add the option to put out scores with a decimal comma.

Make it able to simply put Links to MAL profiles into the malusers.txt
