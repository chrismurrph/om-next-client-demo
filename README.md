# om-next-client-demo
Simple demo of check boxes next to a *graph* (fake graph).

The idea is that all the possible lines you might want to graph are represented as checkboxes.
The checkboxes that are checked will be the lines on the graph. All the re-rendering is done correctly
by Om Next.

Uses the keyword for the fake graph as a follow on read to get it to re-render, while only the checkbox
item that was changed is re-rendered.

All components are React 'controlled components'. This means that the data completely determines what the user sees.

It is a very small program but still has some things in it that are peripheral to Om Next:

1. css
2. use of default-db-format library to check for you that your state is always in the correct format
3. another namespace called help, which can be used for getting difficult mutations working outside of Om Next
4. control over Figwheel usually used with larger programs
5. your own code available from cljs REPL (rarely used)