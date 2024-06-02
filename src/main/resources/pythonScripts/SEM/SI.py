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
    gamma = float(sys.argv[2])
    fraction_infected = float(sys.argv[3])

    print(f"Parameters received - beta: {beta}, gamma: {gamma}, fraction_infected: {fraction_infected}")

    # Network Definition
    g = nx.erdos_renyi_graph(1000, 0.1)
    print("Network created")

    # Model Selection
    model = ep.SIRModel(g)
    print("Model selected")

    # Model Configuration
    config = mc.Configuration()
    config.add_model_parameter('beta', beta)
    config.add_model_parameter('gamma', gamma)
    config.add_model_parameter("fraction_infected", fraction_infected)
    model.set_initial_status(config)
    print("Model configured")

    # Simulation
    iterations = model.iteration_bunch(200)
    trends = model.build_trends(iterations)
    print("Simulation complete")

    # Ensure the plot directory exists
    plot_directory = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../plot")
    os.makedirs(plot_directory, exist_ok=True)

    # Manual Plotting
    output_file(os.path.join(plot_directory, "plot.html"))
    p = figure(title="SIR Model Simulation", x_axis_label='Iteration', y_axis_label='Number of Nodes')

    # Extracting data for plotting
    trends_data = trends[0]['trends']
    susceptible = trends_data['node_count'][0]
    infected = trends_data['node_count'][1]
    removed = trends_data['node_count'][2]

    p.line(list(range(len(susceptible))), susceptible, color='blue', legend_label='Susceptible')
    p.line(list(range(len(infected))), infected, color='green', legend_label='Infected')
    p.line(list(range(len(removed))), removed, color='red', legend_label='Removed')

    save(p)
    print("Plot generated and saved as plot.html")

except Exception as e:
    print(f"An error occurred: {e}")
    sys.exit(1)
