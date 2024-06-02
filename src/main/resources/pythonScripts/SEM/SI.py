import sys
import networkx as nx
import ndlib.models.epidemics as ep
from bokeh.io import output_file, save
from bokeh.plotting import figure
import ndlib.models.ModelConfig as mc
import os

try:
    # Get parameters from command line arguments
    beta = float(sys.argv[1])
    fraction_infected = float(sys.argv[2])

    # Print only the received parameters
    print(f"Parameters received - beta: {beta}, fraction_infected: {fraction_infected}")

    # Network Definition
    g = nx.erdos_renyi_graph(1000, 0.1)

    # Model Selection
    model = ep.SIModel(g)

    # Model Configuration
    config = mc.Configuration()
    config.add_model_parameter('beta', beta)
    config.add_model_parameter("fraction_infected", fraction_infected)
    model.set_initial_status(config)

    # Simulation
    iterations = model.iteration_bunch(200)
    trends = model.build_trends(iterations)

    # Ensure the plot directory exists
    plot_directory = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../plot")
    os.makedirs(plot_directory, exist_ok=True)

    # Manual Plotting
    output_file(os.path.join(plot_directory, "plot.html"))
    p = figure(title="SI Model Simulation", x_axis_label='Iteration', y_axis_label='Number of Nodes')

    # Extracting data for plotting
    trends_data = trends[0]['trends']
    susceptible = trends_data['node_count'][0]
    infected = trends_data['node_count'][1]

    p.line(list(range(len(susceptible))), susceptible, color='blue', legend_label='Susceptible')
    p.line(list(range(len(infected))), infected, color='green', legend_label='Infected')

    save(p)
except Exception as e:
    print(f"An error occurred: {e}")
    sys.exit(1)
