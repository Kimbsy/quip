import plotly.graph_objects as go
import pandas as pd

z_data = pd.read_csv('bench-results.csv')

fig = go.Figure(data=[go.Surface(z=z_data.values)])

fig.update_layout(title='Sprite Tweening Frame Update Performance',
                  scene=dict(xaxis_title='Sprites',
                             yaxis_title='Tweens',
                             zaxis_title='Time (s)'),
                  autosize=False,
                  width=1000, height=1000,
                  margin=dict(l=65, r=50, b=65, t=90))

fig.show()
