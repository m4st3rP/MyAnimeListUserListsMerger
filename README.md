# MyAnimeListUserListsMerger
Creates a CSV that is a merged anime list of multiple MAL users. In this file there should be the name of a MAL user or the link to their profile in each line. It will give out a "MergedLists.txt", that you can import to as example Excel or Calc with "^" as the cell seperator.

A little tutorial on how I use this program:
First I go to overmes' MAL Affinity Search: http://affinity.animesos.net/
There I start multiple searches with differencing shared titles amounts. I suggest starting with about 10% of your completed anime and then increase until the lowest affinity with users you get is 75%.
Then I use the Firefox addon "Link Gopher" to extract all the profile links on the page (for some reason I have to reload the website after the searches).
After that I start the program, paste them into the users text area, enter my MAL username and press start.
Now I wait until it's finished, import the txt file into Excel, use ^ as cell seperator and also set that it's a UTF-8 file.
Optionally I first sort after score count and move every anime with a score count less than 3 to an extra table.
Then I sort them after weighted score.