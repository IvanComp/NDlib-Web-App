import sys
import networkx as nx
import ndlib.models.epidemics as ep
from bokeh.io import export_png
from bokeh.io import output_notebook, show
from ndlib.viz.bokeh.DiffusionTrend import DiffusionTrend
import ndlib.models.ModelConfig as mc

# Get parameters from command line arguments
beta = float(sys.argv[1])
gamma = float(sys.argv[2])
fraction_infected = float(sys.argv[3])

# Network Definition
g = nx.erdos_renyi_graph(1000, 0.1)

# Model Selection
model = ep.SIRModel(g)
# Model Configuration
config = mc.Configuration()
config.add_model_parameter('beta', beta)
config.add_model_parameter('gamma', gamma)
config.add_model_parameter("fraction_infected", fraction_infected)
model.set_initial_status(config)

# Simulation
iterations = model.iteration_bunch(200)
trends = model.build_trends(iterations)

# Visualization
viz = DiffusionTrend(model, trends)
p = viz.plot(width=400, height=400)
export_png(p, filename="plot.png")
