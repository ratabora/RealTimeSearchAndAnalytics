<!DOCTYPE html>
<html lang="en">
    <head>
        <title>SO question 4112686</title>
        <script src="http://code.jquery.com/jquery-latest.min.js"></script>
        <script>
            $(document).ready(function() {                        // When the HTML DOM is ready loading, then execute the following function...
                $('#somebutton').click(function() {               // Locate HTML DOM element with ID "somebutton" and assign the following function to its "click" event...
                    $.get('someservlet', function(responseText) { // Execute Ajax GET request on URL of "someservlet" and execute the following function with Ajax response text...
                        $('div').html(responseText);         // Locate HTML DOM element with ID "somediv" and set its text content with the response text.
                    });
                });
            });
        </script>
        <script type="text/javascript" src="https://www.google.com/jsapi"></script>
		<script type="text/javascript"> 
			google.load("visualization", "1", {packages : [ "corechart" ]});
			google.setOnLoadCallback(drawChart);
			function drawChart() {
				var data = google.visualization.arrayToDataTable(
						[ [ 'Date', 'QTM' ],
							[ 'Sun Dec 05 16:00:00 PST 1999', 16.12 ],
							[ 'Mon Dec 06 16:00:00 PST 1999', 15.69 ],
							[ 'Tue Dec 07 16:00:00 PST 1999', 15.75 ],
							[ 'Wed Dec 08 16:00:00 PST 1999', 14.62 ],
							[ 'Thu Dec 09 16:00:00 PST 1999', 14.75 ],
							[ 'Sun Dec 12 16:00:00 PST 1999', 14.94 ],
							[ 'Mon Dec 13 16:00:00 PST 1999', 13.94 ],
							[ 'Tue Dec 14 16:00:00 PST 1999', 13.81 ],
							[ 'Wed Dec 15 16:00:00 PST 1999', 14.12 ],
							[ 'Thu Dec 16 16:00:00 PST 1999', 14.0 ],
							[ 'Sun Dec 19 16:00:00 PST 1999', 13.5 ],
							[ 'Mon Dec 20 16:00:00 PST 1999', 14.0 ] 
						]
				);
				var options = {title : 'Company Performance'};
				var chart = new google.visualization.LineChart(document.getElementById('chart_div'));chart.draw(data, options);
			}
		</script>
    </head>
    <body>
        <button id="somebutton">press here</button>
        <div id="somediv"></div>
		<div id="chart_div" style="width: 900px; height: 500px;"></div>
    </body>
</html>