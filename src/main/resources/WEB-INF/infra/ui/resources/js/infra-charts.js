// Load the charts
jQuery(document).ready(function() {
  jQuery(".line-chart").each(function() {
    var ctx = this;
    var chart = jQuery(ctx);
    jQuery.get(chart.data('url'), function(data) {
      var myChart = new Chart(ctx, data);
    });
  });
});
